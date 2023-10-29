package com.pgvector

import java.sql.*
import com.pgvector.PGvector
import org.postgresql.PGConnection
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

public class JDBCKotlinTest {
    @Test
    fun example() {
        val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test")

        val setupStmt = conn.createStatement()
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_kotlin_items")

        PGvector.addVectorType(conn)

        val createStmt = conn.createStatement()
        createStmt.executeUpdate("CREATE TABLE jdbc_kotlin_items (id bigserial PRIMARY KEY, embedding vector(3))")

        val insertStmt = conn.prepareStatement("INSERT INTO jdbc_kotlin_items (embedding) VALUES (?), (?), (?), (?)")
        insertStmt.setObject(1, PGvector(floatArrayOf(1.0f, 1.0f, 1.0f)))
        insertStmt.setObject(2, PGvector(floatArrayOf(2.0f, 2.0f, 2.0f)))
        insertStmt.setObject(3, PGvector(floatArrayOf(1.0f, 1.0f, 2.0f)))
        insertStmt.setObject(4, null)
        insertStmt.executeUpdate()

        val neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_kotlin_items ORDER BY embedding <-> ? LIMIT 5")
        neighborStmt.setObject(1, PGvector(floatArrayOf(1.0f, 1.0f, 1.0f)))
        val rs = neighborStmt.executeQuery()
        val ids = mutableListOf<Long>()
        val embeddings = mutableListOf<PGvector?>()
        while (rs.next()) {
            ids.add(rs.getLong("id"))
            embeddings.add(rs.getObject("embedding") as PGvector?)
        }
        assertEquals(arrayListOf(1L, 3L, 2L, 4L), ids)
        assertArrayEquals(floatArrayOf(1.0f, 1.0f, 1.0f), embeddings.get(0)!!.toArray())
        assertArrayEquals(floatArrayOf(1.0f, 1.0f, 2.0f), embeddings.get(1)!!.toArray())
        assertArrayEquals(floatArrayOf(2.0f, 2.0f, 2.0f), embeddings.get(2)!!.toArray())
        assertNull(embeddings.get(3))

        val indexStmt = conn.createStatement()
        indexStmt.executeUpdate("CREATE INDEX ON jdbc_kotlin_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")

        conn.close()
    }
}
