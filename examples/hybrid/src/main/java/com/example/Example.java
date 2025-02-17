package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ai.djl.ModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.pgvector.PGvector;

public class Example {
    public static void main(String[] args) throws IOException, ModelException, SQLException, TranslateException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS documents");

        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE documents (id bigserial PRIMARY KEY, content text, embedding vector(384))");

        ZooModel<String, float[]> model = loadModel("sentence-transformers/multi-qa-MiniLM-L6-cos-v1");

        String[] input = {
            "The dog is barking",
            "The cat is purring",
            "The bear is growling"
        };
        List<float[]> embeddings = embed(model, input);

        for (int i = 0; i < input.length; i++) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO documents (content, embedding) VALUES (?, ?)");
            insertStmt.setString(1, input[i]);
            insertStmt.setObject(2, new PGvector(embeddings.get(i)));
            insertStmt.executeUpdate();
        }

        String query = "growling bear";
        float[] queryEmbedding = embed(model, new String[] {query}).get(0);
        double k = 60;

        PreparedStatement queryStmt = conn.prepareStatement(HYBRID_SQL);
        queryStmt.setObject(1, new PGvector(queryEmbedding));
        queryStmt.setObject(2, new PGvector(queryEmbedding));
        queryStmt.setString(3, query);
        queryStmt.setDouble(4, k);
        queryStmt.setDouble(5, k);
        ResultSet rs = queryStmt.executeQuery();
        while (rs.next()) {
            System.out.println(String.format("document: %d, RRF score: %f", rs.getLong("id"), rs.getDouble("score")));
        }

        conn.close();
    }

    private static ZooModel<String, float[]> loadModel(String id) throws IOException, ModelException {
        return Criteria.builder()
            .setTypes(String.class, float[].class)
            .optModelUrls("djl://ai.djl.huggingface.pytorch/" + id)
            .optEngine("PyTorch")
            .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
            .build()
            .loadModel();
    }

    private static List<float[]> embed(ZooModel<String, float[]> model, String[] input) throws TranslateException {
        Predictor<String, float[]> predictor = model.newPredictor();
        List<float[]> embeddings = new ArrayList<>(input.length);
        for (String text : input) {
            embeddings.add(predictor.predict(text));
        }
        return embeddings;
    }

    public static final String HYBRID_SQL = """
    WITH semantic_search AS (
        SELECT id, RANK () OVER (ORDER BY embedding <=> ?) AS rank
        FROM documents
        ORDER BY embedding <=> ?
        LIMIT 20
    ),
    keyword_search AS (
        SELECT id, RANK () OVER (ORDER BY ts_rank_cd(to_tsvector('english', content), query) DESC)
        FROM documents, plainto_tsquery('english', ?) query
        WHERE to_tsvector('english', content) @@ query
        ORDER BY ts_rank_cd(to_tsvector('english', content), query) DESC
        LIMIT 20
    )
    SELECT
        COALESCE(semantic_search.id, keyword_search.id) AS id,
        COALESCE(1.0 / (? + semantic_search.rank), 0.0) +
        COALESCE(1.0 / (? + keyword_search.rank), 0.0) AS score
    FROM semantic_search
    FULL OUTER JOIN keyword_search ON semantic_search.id = keyword_search.id
    ORDER BY score DESC
    LIMIT 5
    """;
}
