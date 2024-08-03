package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import com.pgvector.PGvector;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class Example {
    public static void main(String[] args) throws SQLException {
        // generate data
        int rows = 1000000;
        int dimensions = 128;
        ArrayList<float[]> embeddings = new ArrayList<>(rows);
        ArrayList<Integer> categories = new ArrayList<>(rows);
        Random rnd = new Random();
        for (int i = 0; i < rows; i++) {
            float[] embedding = new float[dimensions];
            for (int j = 0; j < dimensions; j++) {
                embedding[j] = (float) Math.random();
            }
            embeddings.add(embedding);
            categories.add(rnd.nextInt(100));
        }

        // enable extensions
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_citus");
        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS citus");
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");

        // GUC variables set on the session do not propagate to Citus workers
        // https://github.com/citusdata/citus/issues/462
        // you can either:
        // 1. set them on the system, user, or database and reconnect
        // 2. set them for a transaction with SET LOCAL
        setupStmt.executeUpdate("ALTER DATABASE pgvector_citus SET maintenance_work_mem = '512MB'");
        setupStmt.executeUpdate("ALTER DATABASE pgvector_citus SET hnsw.ef_search = 20");
        conn.close();

        // reconnect for updated GUC variables to take effect
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_citus");
        PGvector.addVectorType(conn);

        System.out.println("Creating distributed table");
        setupStmt = conn.createStatement();
        setupStmt.executeUpdate("DROP TABLE IF EXISTS items");
        setupStmt.executeUpdate(String.format("CREATE TABLE items (id bigserial, embedding vector(%d), category_id bigint, PRIMARY KEY (id, category_id))", dimensions));
        setupStmt.executeUpdate("SET citus.shard_count = 4");
        setupStmt.executeQuery("SELECT create_distributed_table('items', 'category_id')");

        System.out.println("Loading data in parallel");
        CopyManager copyManager = new CopyManager((BaseConnection) conn);
        // TODO use binary format
        CopyIn copyIn = copyManager.copyIn("COPY items (embedding, category_id) FROM STDIN");
        for (int i = 0; i < rows; i++) {
            PGvector embedding = new PGvector(embeddings.get(i));
            byte[] bytes = String.format("%s\t%d\n", embedding.getValue(), categories.get(i)).getBytes();
            copyIn.writeToCopy(bytes, 0, bytes.length);
        }
        copyIn.endCopy();

        System.out.println("Creating index in parallel");
        Statement createIndexStmt = conn.createStatement();
        createIndexStmt.executeUpdate("CREATE INDEX ON items USING hnsw (embedding vector_l2_ops)");

        System.out.println("Running distributed queries");
        for (int i = 0; i < 10; i++) {
            PreparedStatement queryStmt = conn.prepareStatement("SELECT id FROM items ORDER BY embedding <-> ? LIMIT 10");
            queryStmt.setObject(1, new PGvector(embeddings.get(rnd.nextInt(rows))));
            ResultSet rs = queryStmt.executeQuery();
            ArrayList<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
            System.out.println(ids.toString());
        }

        conn.close();
    }
}
