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
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PGBinaryObject;

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
        CopyIn copyIn = copyManager.copyIn("COPY items (embedding) FROM STDIN WITH (FORMAT BINARY)");

        // write header
        // https://www.postgresql.org/docs/current/sql-copy.html
        byte[] buffer = new byte[32768];
        byte[] signature = new byte[] {80, 71, 67, 79, 80, 89, 10, (byte)255, 13, 10, 0};
        System.arraycopy(signature, 0, buffer, 0, signature.length);
        ByteConverter.int4(buffer, 11, 0);
        ByteConverter.int4(buffer, 15, 0);
        copyIn.writeToCopy(buffer, 0, 19);

        for (int i = 0; i < rows; i++) {
            PGBinaryObject[] values = {new PGvector(embeddings.get(i))};

            // write row
            int pos = 0;
            ByteConverter.int2(buffer, pos, values.length);
            pos += 2;
            for (int j = 0; j < values.length; j++) {
                PGBinaryObject value = values[j];
                int len = value.lengthInBytes();
                ByteConverter.int4(buffer, pos, len);
                pos += 4;
                value.toBytes(buffer, pos);
                pos += len;
            }
            copyIn.writeToCopy(buffer, 0, pos);

            // show progress
            if (i % 10000 == 0) {
                System.out.print(".");
            }
        }

        // write trailer
        ByteConverter.int2(buffer, 0, -1);
        copyIn.writeToCopy(buffer, 0, 2);
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
