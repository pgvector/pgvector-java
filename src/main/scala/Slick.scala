import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.PostgresProfile.api._

class Items(tag: Tag) extends Table[(String)](tag, "slick_items") {
  def embedding = column[String]("embedding", O.SqlType("vector(3)"))
  def * = (embedding)
}

object Pgvector {
  def toString(v: List[Float]) = {
    "[" + v.mkString(",") + "]"
  }
}

object Slick {
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
        val embedding1 = Pgvector.toString(List(1, 1, 1))
        val embedding2 = Pgvector.toString(List(2, 2, 2))
        val embedding3 = Pgvector.toString(List(1, 1, 2))
        db.run(sqlu"INSERT INTO slick_items (embedding) VALUES ($embedding1::vector), ($embedding2::vector), ($embedding3::vector)")
      }.flatMap { _ =>
        // select
        val embedding = Pgvector.toString(List(1, 1, 1))
        db.run(sql"SELECT * FROM slick_items ORDER BY embedding <-> $embedding::vector LIMIT 5".as[(String)].map(println))
      }.flatMap { _ =>
        // index
        db.run(sqlu"CREATE INDEX slick_index ON slick_items USING ivfflat (embedding vector_l2_ops)")
      }

      Await.result(resultFuture, Duration.Inf)
    } finally db.close
  }
}
