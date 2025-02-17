// good resources
// https://opensearch.org/blog/improving-document-retrieval-with-sparse-semantic-encoders/
// https://huggingface.co/opensearch-project/opensearch-neural-sparse-encoding-v1
//
// run with
// text-embeddings-router --model-id opensearch-project/opensearch-neural-sparse-encoding-v1 --pooling splade

package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgvector.PGsparsevec;
import com.pgvector.PGvector;

public class Example {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS documents");

        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE documents (id bigserial PRIMARY KEY, content text, embedding sparsevec(30522))");

        String[] input = {
            "The dog is barking",
            "The cat is purring",
            "The bear is growling"
        };
        List<Map<Integer, Float>> embeddings = embed(input);

        for (int i = 0; i < input.length; i++) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO documents (content, embedding) VALUES (?, ?)");
            insertStmt.setString(1, input[i]);
            insertStmt.setObject(2, new PGsparsevec(embeddings.get(i), 30522));
            insertStmt.executeUpdate();
        }

        String query = "forest";
        Map<Integer, Float> queryEmbedding = embed(new String[] { query }).get(0);
        PreparedStatement neighborStmt = conn.prepareStatement("SELECT content FROM documents ORDER BY embedding <#> ? LIMIT 5");
        neighborStmt.setObject(1, new PGsparsevec(queryEmbedding, 30522));
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("content"));
        }

        conn.close();
    }

    private static List<Map<Integer, Float>> embed(String[] inputs) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        for (String v : inputs) {
            root.withArray("inputs").add(v);
        }
        String json = mapper.writeValueAsString(root);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:3000/embed_sparse"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        List<Map<Integer, Float>> embeddings = new ArrayList<>();
        for (JsonNode n : mapper.readTree(response.body())) {
            Map<Integer, Float> embedding = new HashMap<Integer, Float>();
            for (JsonNode v : n) {
                int index = v.get("index").asInt();
                float value = (float) v.get("value").asDouble();
                embedding.put(Integer.valueOf(index), Float.valueOf(value));
            }
            embeddings.add(embedding);
        }
        return embeddings;
    }
}
