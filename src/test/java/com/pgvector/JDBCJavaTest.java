package com.pgvector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.pgvector.PGvector;
import org.postgresql.PGConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JDBCJavaTest {
    @Test
    void testReadText() throws SQLException {
        example(false);
    }

    @Test
    void testReadBinary() throws SQLException {
        example(true);
    }

    void example(boolean readBinary) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_java_test");
        if (readBinary) {
            conn.unwrap(PGConnection.class).setPrepareThreshold(-1);
        }

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS jdbc_items");

        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE jdbc_items (id bigserial PRIMARY KEY, embedding vector(3))");

        PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO jdbc_items (embedding) VALUES (?), (?), (?), (?)");
        insertStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
        insertStmt.setObject(2, new PGvector(new float[] {2, 2, 2}));
        insertStmt.setObject(3, new PGvector(new float[] {1, 1, 2}));
        insertStmt.setObject(4, null);
        insertStmt.executeUpdate();

        PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM jdbc_items ORDER BY embedding <-> ? LIMIT 5");
        neighborStmt.setObject(1, new PGvector(new float[] {1, 1, 1}));
        ResultSet rs = neighborStmt.executeQuery();
        List<Long> ids = new ArrayList<>();
        List<PGvector> embeddings = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getLong("id"));
            embeddings.add((PGvector) rs.getObject("embedding"));
        }
        assertArrayEquals(new Long[] {1L, 3L, 2L, 4L}, ids.toArray());
        assertArrayEquals(new float[] {1, 1, 1}, embeddings.get(0).toArray());
        assertArrayEquals(new float[] {1, 1, 2}, embeddings.get(1).toArray());
        assertArrayEquals(new float[] {2, 2, 2}, embeddings.get(2).toArray());
        assertNull(embeddings.get(3));

        Statement indexStmt = conn.createStatement();
        indexStmt.executeUpdate("CREATE INDEX ON jdbc_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");

        conn.close();
    }
}
