package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.pgvector.PGvector;
import org.ankane.disco.Data;
import org.ankane.disco.Dataset;
import org.ankane.disco.Recommender;

public class Example {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");
        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        PGvector.addVectorType(conn);

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("DROP TABLE IF EXISTS users");
        createStmt.executeUpdate("DROP TABLE IF EXISTS movies");
        createStmt.executeUpdate("CREATE TABLE users (id integer PRIMARY KEY, factors vector(20))");
        createStmt.executeUpdate("CREATE TABLE movies (name text PRIMARY KEY, factors vector(20))");

        Dataset<Integer, String> data = Data.loadMovieLens();
        Recommender<Integer, String> recommender = Recommender
            .builder()
            .factors(20)
            .fitExplicit(data);

        for (Integer userId : recommender.userIds()) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (id, factors) VALUES (?, ?)");
            insertStmt.setInt(1, userId);
            insertStmt.setObject(2, new PGvector(recommender.userFactors(userId).get()));
            insertStmt.executeUpdate();
        }

        for (String itemId : recommender.itemIds()) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO movies (name, factors) VALUES (?, ?)");
            insertStmt.setString(1, itemId);
            insertStmt.setObject(2, new PGvector(recommender.itemFactors(itemId).get()));
            insertStmt.executeUpdate();
        }

        String movie = "Star Wars (1977)";
        System.out.printf("Item-based recommendations for %s\n", movie);
        PreparedStatement neighborStmt = conn.prepareStatement("SELECT name FROM movies WHERE name != ? ORDER BY factors <=> (SELECT factors FROM movies WHERE name = ?) LIMIT 5");
        neighborStmt.setString(1, movie);
        neighborStmt.setString(2, movie);
        ResultSet rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println("- " + rs.getString("name"));
        }

        int userId = 123;
        System.out.printf("\nUser-based recommendations for user %d\n", userId);
        neighborStmt = conn.prepareStatement("SELECT name FROM movies ORDER BY factors <#> (SELECT factors FROM users WHERE id = ?) LIMIT 5");
        neighborStmt.setInt(1, userId);
        rs = neighborStmt.executeQuery();
        while (rs.next()) {
            System.out.println("- " + rs.getString("name"));
        }

        conn.close();
    }
}
