package com.pgvector;

import java.math.BigInteger;
import java.sql.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.pgvector.PGvector;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HibernateTest {
    @Test
    public void example() throws SQLException {
        // disable logging
        System.setProperty("org.jboss.logging.provider", "slf4j");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
        SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.createNativeQuery("CREATE EXTENSION IF NOT EXISTS vector").executeUpdate();
        session.createNativeQuery("DROP TABLE IF EXISTS hibernate_items").executeUpdate();
        session.createNativeQuery("CREATE TABLE hibernate_items (id bigserial PRIMARY KEY, embedding vector(3))").executeUpdate();

        session.createNativeQuery("INSERT INTO hibernate_items (embedding) VALUES (CAST(? AS vector)), (CAST(? AS vector)), (CAST(? AS vector)), (NULL)")
            .setParameter(1, (new PGvector(new float[] {1, 1, 1})).getValue())
            .setParameter(2, (new PGvector(new float[] {2, 2, 2})).getValue())
            .setParameter(3, (new PGvector(new float[] {1, 1, 2})).getValue())
            .executeUpdate();

        @SuppressWarnings("unchecked")
        List<Object[]> items = session
            .createNativeQuery("SELECT id, CAST(embedding AS text) FROM hibernate_items ORDER BY embedding <-> CAST(? AS vector) LIMIT 5")
            .setParameter(1, (new PGvector(new float[] {1, 1, 1})).getValue())
            .list();
        List<Long> ids = new ArrayList<>();
        List<PGvector> embeddings = new ArrayList<>();
        for (Object[] item : items) {
            ids.add(Long.valueOf(((BigInteger) item[0]).longValue()));
            embeddings.add(item[1] == null ? null : new PGvector((String) item[1]));
        }
        assertArrayEquals(new Long[]{1L, 3L, 2L, 4L}, ids.toArray());
        assertArrayEquals(new float[] {1, 1, 1}, embeddings.get(0).toArray());
        assertArrayEquals(new float[] {1, 1, 2}, embeddings.get(1).toArray());
        assertArrayEquals(new float[] {2, 2, 2}, embeddings.get(2).toArray());
        assertNull(embeddings.get(3));

        session.getTransaction().commit();
        session.close();
    }
}
