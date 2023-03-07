import java.sql.DriverManager
import com.pgvector.PGvector

object JDBCScala {
  def example(): Unit = {
    val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test")

    val setupStmt = conn.createStatement()
    setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
    setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items")

    PGvector.addVectorType(conn)

    val createStmt = conn.createStatement()
    createStmt.executeUpdate("CREATE TABLE jdbc_items (embedding vector(3))")

    val insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?), (?), (?), (?)")
    insertStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
    insertStmt.setObject(2, new PGvector(Array[Float](2, 2, 2)))
    insertStmt.setObject(3, new PGvector(Array[Float](1, 1, 2)))
    insertStmt.setObject(4, new PGvector())
    insertStmt.executeUpdate()

    val neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ? LIMIT 5")
    neighborStmt.setObject(1, new PGvector(Array[Float](1, 1, 1)))
    val rs = neighborStmt.executeQuery()
    while (rs.next()) {
      println(rs.getObject("embedding").asInstanceOf[PGvector])
    }

    val indexStmt = conn.createStatement()
    indexStmt.executeUpdate("CREATE INDEX jdbc_index ON jdbc_items USING ivfflat (embedding vector_l2_ops)")

    conn.close()
  }
}
