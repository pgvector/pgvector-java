package com.pgvector

import groovy.sql.Sql
import com.pgvector.PGvector
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

public class GroovySqlTest {
    @Test
    void example() {
        def sql = Sql.newInstance("jdbc:postgresql://localhost:5432/pgvector_java_test")

        sql.execute "CREATE EXTENSION IF NOT EXISTS vector"
        sql.execute "DROP TABLE IF EXISTS groovy_sql_items"

        sql.execute "CREATE TABLE groovy_sql_items (id bigserial PRIMARY KEY, embedding vector(3))"

        def params = [
            new PGvector([1, 1, 1] as float[]),
            new PGvector([2, 2, 2] as float[]),
            new PGvector([1, 1, 2] as float[]),
            null
        ]
        sql.executeInsert "INSERT INTO groovy_sql_items (embedding) VALUES (?), (?), (?), (?)", params

        def embedding = new PGvector([1, 1, 1] as float[])
        def ids = []
        def embeddings = []
        sql.eachRow("SELECT * FROM groovy_sql_items ORDER BY embedding <-> ? LIMIT 5", [embedding]) { row ->
            ids.add(row.id)
            embeddings.add(row.embedding == null ? null : new PGvector(row.embedding.getValue()))
        }
        assertArrayEquals([1, 3, 2, 4] as Long[], ids.toArray())
        assertArrayEquals([1, 1, 1] as float[], embeddings.get(0).toArray())
        assertArrayEquals([1, 1, 2] as float[], embeddings.get(1).toArray())
        assertArrayEquals([2, 2, 2] as float[], embeddings.get(2).toArray())
        assertNull(embeddings.get(3))

        sql.execute "CREATE INDEX ON groovy_sql_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)"

        sql.close()
    }
}
