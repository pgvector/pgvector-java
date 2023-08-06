package com.pgvector;

import java.sql.*;
import java.util.List;
import com.pgvector.PGvector;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class Hibernate {
    public static void example() throws SQLException {
        // disable logging
        System.setProperty("org.jboss.logging.provider", "slf4j");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
        SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.createNativeQuery("CREATE EXTENSION IF NOT EXISTS vector", Object.class).executeUpdate();
        session.createNativeQuery("DROP TABLE IF EXISTS hibernate_items", Object.class).executeUpdate();
        session.createNativeQuery("CREATE TABLE hibernate_items (id bigserial PRIMARY KEY, embedding vector(3))", Object.class).executeUpdate();

        session.createNativeQuery("INSERT INTO hibernate_items (embedding) VALUES (CAST(? AS vector)), (CAST(? AS vector)), (CAST(? AS vector)), (?)", Object.class)
            .setParameter(1, (new PGvector(new float[] {1, 1, 1})).getValue())
            .setParameter(2, (new PGvector(new float[] {2, 2, 2})).getValue())
            .setParameter(3, (new PGvector(new float[] {1, 1, 2})).getValue())
            .setParameter(4, null)
            .executeUpdate();

        List<Object[]> items = session
            .createNativeQuery("SELECT * FROM hibernate_items ORDER BY embedding <-> CAST(? AS vector) LIMIT 5", Object[].class)
            .setParameter(1, (new PGvector(new float[] {1, 1, 1})).getValue())
            .list();
        for (Object[] item : items) {
            System.out.println(item[0]);
            System.out.println(item[1]);
        }

        session.getTransaction().commit();
        session.close();
    }
}
