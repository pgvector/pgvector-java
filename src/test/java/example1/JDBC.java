package example1;

import com.pgvector.PGvector;
import java.sql.*;
import java.util.Arrays;

public class JDBC {
    public static void example() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items");

        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE jdbc_items (embedding vector(3))");

        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?), (?), (?), (?)");
        insertStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
        insertStmt.setObject(2, new PGvector(new float[] {2, 2, 2}));
        insertStmt.setObject(3, new PGvector(new float[] {1, 1, 2}));
        insertStmt.setObject(4, new PGvector());
        insertStmt.executeUpdate();

        PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ? LIMIT 5");
        neighborStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println((PGvector) rs.getObject(1));
        }

        Statement indexStmt = conn.createStatement();
        indexStmt.executeUpdate("CREATE INDEX jdbc_index ON jdbc_items USING ivfflat (embedding vector_l2_ops)");

        conn.close();
    }
}
