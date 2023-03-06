import java.sql.*;

class JDBCJava {
    public static void example() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items");

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE jdbc_items (embedding vector(3))");

        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?::vector), (?::vector), (?::vector)");
        insertStmt.setString(1, PgvectorJava.toString(new float[] {1, 1, 1}));
        insertStmt.setString(2, PgvectorJava.toString(new float[] {2, 2, 2}));
        insertStmt.setString(3, PgvectorJava.toString(new float[] {1, 1, 2}));
        insertStmt.executeUpdate();

        PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ?::vector LIMIT 5");
        neighborStmt.setString(1, PgvectorJava.toString(new float[] {1, 1, 1}));
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("embedding"));
        }

        Statement indexStmt = conn.createStatement();
        indexStmt.executeUpdate("CREATE INDEX jdbc_index ON jdbc_items USING ivfflat (embedding vector_l2_ops)");

        conn.close();
    }
}
