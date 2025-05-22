package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.pgvector.PGvector;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class Example {
    public static void main(String[] args) throws SQLException {
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
            PGvector embedding = new PGvector(embeddings.get(i));
            byte[] bytes = (embedding.getValue() + "\n").getBytes();
            copyIn.writeToCopy(bytes, 0, bytes.length);

            if (i % 10000 == 0) {
                System.out.print(".");
            }
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
