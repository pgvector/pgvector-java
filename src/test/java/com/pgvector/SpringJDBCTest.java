package com.pgvector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.pgvector.PGvector;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpringJDBCTest {
    @Test
    public void example() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/pgvector_java_test");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("DROP TABLE IF EXISTS spring_items");

        jdbcTemplate.execute("CREATE TABLE spring_items (id bigserial PRIMARY KEY, embedding vector(3))");

        Object[] insertParams = new Object[] {
            new PGvector(new float[] {1, 1, 1}),
            new PGvector(new float[] {2, 2, 2}),
            new PGvector(new float[] {1, 1, 2}),
            null
        };
        jdbcTemplate.update("INSERT INTO spring_items (embedding) VALUES (?), (?), (?), (?)", insertParams);

        Object[] neighborParams = new Object[] { new PGvector(new float[] {1, 1, 1}) };
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM spring_items ORDER BY embedding <-> ? LIMIT 5", neighborParams);
        List<Long> ids = new ArrayList<>();
        List<PGvector> embeddings = new ArrayList<>();
        for (Map row : rows) {
            ids.add((Long) row.get("id"));
            embeddings.add(row.get("embedding") == null ? null : new PGvector(((PGobject) row.get("embedding")).getValue()));
        }
        assertArrayEquals(new Long[] {1L, 3L, 2L, 4L}, ids.toArray());
        assertArrayEquals(new float[] {1, 1, 1}, embeddings.get(0).toArray());
        assertArrayEquals(new float[] {1, 1, 2}, embeddings.get(1).toArray());
        assertArrayEquals(new float[] {2, 2, 2}, embeddings.get(2).toArray());
        assertNull(embeddings.get(3));

        jdbcTemplate.execute("CREATE INDEX ON spring_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");
    }
}
