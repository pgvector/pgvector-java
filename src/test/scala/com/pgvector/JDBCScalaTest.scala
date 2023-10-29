package com.pgvector

import java.sql.DriverManager
import com.pgvector.PGvector
import scala.collection.mutable.ArrayBuffer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions._

class JDBCScalaTest {
  @Test
  def example(): Unit = {
    val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test")

    val setupStmt = conn.createStatement()
    setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
    setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_scala_items")

    PGvector.addVectorType(conn)

    val createStmt = conn.createStatement()
    createStmt.executeUpdate("CREATE TABLE jdbc_scala_items (id bigserial PRIMARY KEY, embedding vector(3))")

    val insertStmt = conn.prepareStatement("INSERT INTO jdbc_scala_items (embedding) VALUES (?), (?), (?), (?)")
    insertStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
    insertStmt.setObject(2, new PGvector(Array[Float](2, 2, 2)))
    insertStmt.setObject(3, new PGvector(Array[Float](1, 1, 2)))
    insertStmt.setObject(4, null)
    insertStmt.executeUpdate()

    val neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_scala_items ORDER BY embedding <-> ? LIMIT 5")
    neighborStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
    val rs = neighborStmt.executeQuery()
    val ids = ArrayBuffer[Long]()
    val embeddings = ArrayBuffer[PGvector]()
    while (rs.next()) {
      ids += rs.getLong("id")
      embeddings += rs.getObject("embedding").asInstanceOf[PGvector]
    }
    assertArrayEquals(Array[Long](1, 3, 2, 4), ids.toArray)
    assertArrayEquals(Array[Float](1, 1, 1), embeddings(0).toArray())
    assertArrayEquals(Array[Float](1, 1, 2), embeddings(1).toArray())
    assertArrayEquals(Array[Float](2, 2, 2), embeddings(2).toArray())
    assertNull(embeddings(3))

    val indexStmt = conn.createStatement()
    indexStmt.executeUpdate("CREATE INDEX ON jdbc_scala_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")

    conn.close()
  }
}
