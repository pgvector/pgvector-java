package example2

import java.sql.DriverManager

object JDBC {
  def example(): Unit = {
    val conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test")

    val setupStmt = conn.createStatement()
    setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector")
    setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items")

    val createStmt = conn.createStatement()
    createStmt.executeUpdate("CREATE TABLE jdbc_items (embedding vector(3))")

    val insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?::vector), (?::vector), (?::vector)")
    insertStmt.setString(1, Pgvector.toString(Array(1, 1, 1)))
    insertStmt.setString(2, Pgvector.toString(Array(2, 2, 2)))
    insertStmt.setString(3, Pgvector.toString(Array(1, 1, 2)))
    insertStmt.executeUpdate()

    val neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ?::vector LIMIT 5")
    neighborStmt.setString(1, Pgvector.toString(Array(1, 1, 1)))
    val rs = neighborStmt.executeQuery()
    while (rs.next()) {
      println(Pgvector.parse(rs.getString("embedding")).toList)
    }

    val indexStmt = conn.createStatement()
    indexStmt.executeUpdate("CREATE INDEX jdbc_index ON jdbc_items USING ivfflat (embedding vector_l2_ops)")

    conn.close()
  }
}
