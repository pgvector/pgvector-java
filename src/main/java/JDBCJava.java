import java.sql.*;

class JDBCJava {
    public static void example() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items");

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE jdbc_items (embedding vector(3))");

        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?::vector), (?::vector), (?::vector)");
        insertStmt.setString(1, "[1,1,1]");
        insertStmt.setString(2, "[2,2,2]");
        insertStmt.setString(3, "[1,1,2]");
        insertStmt.executeUpdate();

        ResultSet rs = stmt.executeQuery("SELECT * FROM jdbc_items ORDER BY embedding <-> '[1,1,1]' LIMIT 5");
        while (rs.next()) {
            System.out.println(rs.getString("embedding"));
        }

        stmt.executeUpdate("CREATE INDEX jdbc_index ON jdbc_items USING ivfflat (embedding vector_l2_ops)");

        conn.close();
    }
}
