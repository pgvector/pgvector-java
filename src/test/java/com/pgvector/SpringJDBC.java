package com.pgvector;

import java.sql.*;
import java.util.List;
import java.util.Map;
import com.pgvector.PGvector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SpringJDBC {
    public static void example() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/pgvector_java_test");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("DROP TABLE IF EXISTS spring_items");

        jdbcTemplate.execute("CREATE TABLE spring_items (embedding vector(3))");

        Object[] insertParams = new Object[] {
            new PGvector(new float[] {1, 1, 1}),
            new PGvector(new float[] {2, 2, 2}),
            new PGvector(new float[] {1, 1, 2}),
            new PGvector()
        };
        jdbcTemplate.update("INSERT INTO spring_items (embedding) VALUES (?), (?), (?), (?)", insertParams);

        Object[] neighborParams = new Object[] { new PGvector(new float[] {1, 1, 1}) };
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM spring_items ORDER BY embedding <-> ? LIMIT 5", neighborParams);
        for (Map row : rows) {
            System.out.println(row.get("embedding"));
        }

        jdbcTemplate.execute("CREATE INDEX spring_index ON spring_items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100)");
    }
}
