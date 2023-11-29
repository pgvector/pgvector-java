package com.pgvector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgvector.PGvector;
import org.postgresql.PGConnection;
import org.junit.jupiter.api.Test;

public class OpenAITest {
    @Test
    void example() throws IOException, InterruptedException, SQLException {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            return;
        }

        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS documents");

        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE documents (id bigserial PRIMARY KEY, content text, embedding vector(1536))");

        String[] input = {
            "The dog is barking",
            "The cat is purring",
            "The bear is growling"
        };
        List<float[]> embeddings = fetchEmbeddings(input, apiKey);

        for (int i = 0; i < input.length; i++) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO documents (content, embedding) VALUES (?, ?)");
            insertStmt.setString(1, input[i]);
            insertStmt.setObject(2, new PGvector(embeddings.get(i)));
            insertStmt.executeUpdate();
        }

        long documentId = 2;
        PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM documents WHERE id != ? ORDER BY embedding <=> (SELECT embedding FROM documents WHERE id = ?) LIMIT 5");
        neighborStmt.setObject(1, documentId);
        neighborStmt.setObject(2, documentId);
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("content"));
        }

        conn.close();
    }

    private List<float[]> fetchEmbeddings(String[] input, String apiKey) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        for (String v : input) {
            root.withArray("input").add(v);
        }
        root.put("model", "text-embedding-ada-002");
        String json = mapper.writeValueAsString(root);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/embeddings"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        List<float[]> embeddings = new ArrayList<float[]>();
        for (JsonNode n : mapper.readTree(response.body()).get("data")) {
            float[] embedding = new float[1536];
            int i = 0;
            for (JsonNode v : n.get("embedding")) {
                embedding[i++] = (float) v.asDouble();
            }
            embeddings.add(embedding);
        }
        return embeddings;
    }
}
