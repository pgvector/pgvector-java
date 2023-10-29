package com.pgvector

import java.sql.*
import com.pgvector.PGvector
import org.postgresql.PGConnection
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

public class JDBCGroovyTest {
    @Test
    void example() throws SQLException {
        def conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test")

        def setupStmt = conn.createStatement()
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items")

        PGvector.addVectorType(conn)

        def createStmt = conn.createStatement()
        createStmt.executeUpdate("CREATE TABLE jdbc_items (id bigserial PRIMARY KEY, embedding vector(3))")

        def insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?), (?), (?), (?)")
        insertStmt.setObject(1, new PGvector([1, 1, 1] as float[]))
        insertStmt.setObject(2, new PGvector([2, 2, 2] as float[]))
        insertStmt.setObject(3, new PGvector([1, 1, 2] as float[]))
        insertStmt.setObject(4, null)
        insertStmt.executeUpdate()

        def neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ? LIMIT 5")
        neighborStmt.setObject(1, new PGvector([1, 1, 1] as float[]))
        def rs = neighborStmt.executeQuery()
        def ids = new ArrayList<Long>()
        def embeddings = new ArrayList<PGvector>()
        while (rs.next()) {
            ids.add(rs.getLong("id"))
            embeddings.add((PGvector) rs.getObject("embedding"))
        }
        assertArrayEquals([1, 3, 2, 4] as Long[], ids.toArray())
        assertArrayEquals([1, 1, 1] as float[], embeddings.get(0).toArray())
        assertArrayEquals([1, 1, 2] as float[], embeddings.get(1).toArray())
        assertArrayEquals([2, 2, 2] as float[], embeddings.get(2).toArray())
        assertNull(embeddings.get(3))

        def indexStmt = conn.createStatement()
        indexStmt.executeUpdate("CREATE INDEX ON jdbc_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")

        conn.close()
    }
}
