package com.pgvector;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.pgvector.PGvector;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.junit.jupiter.api.Test;

public class LoadingTest {
    @Test
    void example() throws SQLException, UnsupportedEncodingException {
        if (System.getenv("TEST_LOADING") == null) {
            return;
        }

        // generate random data
        int rows = 1000000;
        int dimensions = 128;
        ArrayList<float[]> embeddings = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            float[] embedding = new float[dimensions];
            for (int j = 0; j < dimensions; j++) {
                embedding[j] = (float) Math.random();
            }
            embeddings.add(embedding);
        }

        // enable extension
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");
        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        PGvector.addVectorType(conn);

        // create table
        setupStmt.executeUpdate("DROP TABLE IF EXISTS items");
        setupStmt.executeUpdate("CREATE TABLE items (id bigserial, embedding vector(128))");

        // load data
        System.out.println("Loading 1000000 rows");

        CopyManager copyManager = new CopyManager((BaseConnection) conn);
        // TODO use binary format
        CopyIn copyIn = copyManager.copyIn("COPY items (embedding) FROM STDIN");
        for (int i = 0; i < rows; i++) {
            if (i % 10000 == 0) {
                System.out.print(".");
            }

            PGvector embedding = new PGvector(embeddings.get(i));
            byte[] bytes = (embedding.getValue() + "\n").getBytes("UTF-8");
            copyIn.writeToCopy(bytes, 0, bytes.length);
        }
        copyIn.endCopy();

        System.out.println("\nSuccess!");

        // create any indexes *after* loading initial data (skipping for this example)
        boolean createIndex = false;
        if (createIndex) {
            System.out.println("Creating index");
            Statement createIndexStmt = conn.createStatement();
            createIndexStmt.executeUpdate("SET maintenance_work_mem = '8GB'");
            createIndexStmt.executeUpdate("SET max_parallel_maintenance_workers = 7");
            createIndexStmt.executeUpdate("CREATE INDEX ON items USING hnsw (embedding vector_cosine_ops)");
        }

        // update planner statistics for good measure
        Statement analyzeStmt = conn.createStatement();
        analyzeStmt.executeUpdate("ANALYZE items");

        conn.close();
    }
}
