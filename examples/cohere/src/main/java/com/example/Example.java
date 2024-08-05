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
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pgvector.PGbit;

public class Example {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        String apiKey = System.getenv("CO_API_KEY");
        if (apiKey == null) {
            System.out.println("Set CO_API_KEY");
            System.exit(1);
        }

        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS documents");

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE documents (id bigserial PRIMARY KEY, content text, embedding bit(1024))");

        String[] input = {
            "The dog is barking",
            "The cat is purring",
            "The bear is growling"
        };
        List<byte[]> embeddings = fetchEmbeddings(input, "search_document", apiKey);
        for (int i = 0; i < input.length; i++) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO documents (content, embedding) VALUES (?, ?)");
            insertStmt.setString(1, input[i]);
            insertStmt.setObject(2, new PGbit(embeddings.get(i)));
            insertStmt.executeUpdate();
        }

        String query = "forest";
        byte[] queryEmbedding = fetchEmbeddings(new String[] {query}, "search_query", apiKey).get(0);
        PreparedStatement neighborStmt = conn.prepareStatement("SELECT * FROM documents ORDER BY embedding <~> ? LIMIT 5");
        neighborStmt.setObject(1, new PGbit(queryEmbedding));
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("content"));
        }

        conn.close();
    }

    // https://docs.cohere.com/reference/embed
    private static List<byte[]> fetchEmbeddings(String[] texts, String inputType, String apiKey) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        for (String v : texts) {
            root.withArray("texts").add(v);
        }
        root.put("model", "embed-english-v3.0");
        root.put("input_type", inputType);
        root.withArray("embedding_types").add("binary");
        String json = mapper.writeValueAsString(root);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.cohere.com/v1/embed"))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        List<byte[]> embeddings = new ArrayList<>();
        for (JsonNode n : mapper.readTree(response.body()).get("embeddings").get("binary")) {
            byte[] embedding = new byte[n.size()];
            int i = 0;
            for (JsonNode v : n) {
                embedding[i++] = (byte) v.asInt();
            }
            embeddings.add(embedding);
        }
        return embeddings;
    }
}
