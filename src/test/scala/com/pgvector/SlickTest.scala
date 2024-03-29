package com.pgvector

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._
import com.pgvector.PGvector
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals

class Items(tag: Tag) extends Table[(Int, String)](tag, "slick_items") {
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def embedding = column[String]("embedding", O.SqlType("vector(3)"))
  def * = (id, embedding)
}

class SlickTest {
  @Test
  def example(): Unit = {
    val db = Database.forURL("jdbc:postgresql://localhost:5432/pgvector_java_test", driver="org.postgresql.Driver")

    try {
      val items = TableQuery[Items]
      val schema = items.schema
      val setup = DBIO.seq(
        sqlu"CREATE EXTENSION IF NOT EXISTS vector",
        schema.dropIfExists,
        schema.create,
      )
      val setupFuture = db.run(setup)

      val resultFuture = setupFuture.flatMap { _ =>
        // insert
        val embedding1 = new PGvector(Array[Float](1, 1, 1)).toString
        val embedding2 = new PGvector(Array[Float](2, 2, 2)).toString
        val embedding3 = new PGvector(Array[Float](1, 1, 2)).toString
        db.run(sqlu"INSERT INTO slick_items (embedding) VALUES ($embedding1::vector), ($embedding2::vector), ($embedding3::vector)")
      }.flatMap { _ =>
        // select
        val embedding = new PGvector(Array[Float](1, 1, 1)).toString
        db.run(sql"SELECT * FROM slick_items ORDER BY embedding <-> $embedding::vector LIMIT 5".as[(Int, String)])
      }.flatMap { rows =>
        // check
        val ids = rows.map(r => r._1)
        val embeddings = rows.map(r => new PGvector(r._2))
        assertEquals(List(1, 3, 2), ids)
        assertArrayEquals(Array[Float](1, 1, 1), embeddings(0).toArray)
        assertArrayEquals(Array[Float](1, 1, 2), embeddings(1).toArray)
        assertArrayEquals(Array[Float](2, 2, 2), embeddings(2).toArray)

        // index
        db.run(sqlu"CREATE INDEX ON slick_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)")
      }

      Await.result(resultFuture, Duration.Inf)
    } finally db.close
  }
}
