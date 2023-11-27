package com.pgvector;

import jakarta.persistence.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Entity
@Table(name = "hibernate_items")
class Item {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 3)
    private float[] embedding;

    public Long getId() {
        return id;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}

public class HibernateTest {
    @Test
    void example() throws SQLException {
        // disable logging
        System.setProperty("org.jboss.logging.provider", "slf4j");

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        Item item1 = new Item();
        item1.setEmbedding(new float[] {1, 1, 1});
        entityManager.persist(item1);

        Item item2 = new Item();
        item2.setEmbedding(new float[] {2, 2, 2});
        entityManager.persist(item2);

        Item item3 = new Item();
        item3.setEmbedding(new float[] {1, 1, 2});
        entityManager.persist(item3);

        List<Item> items = entityManager
            .createQuery("FROM Item ORDER BY l2_distance(embedding, :embedding) LIMIT 5", Item.class)
            .setParameter("embedding", new float[] {1, 1, 1})
            .getResultList();
        assertArrayEquals(new Long[] {1L, 3L, 2L}, items.stream().map(v -> v.getId()).toArray());
        assertArrayEquals(new float[] {1, 1, 1}, items.get(0).getEmbedding());
        assertArrayEquals(new float[] {1, 1, 2}, items.get(1).getEmbedding());
        assertArrayEquals(new float[] {2, 2, 2}, items.get(2).getEmbedding());

        entityManager.getTransaction().commit();
    }
}
