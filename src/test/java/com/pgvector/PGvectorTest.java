package com.pgvector;

import java.sql.SQLException;
import com.pgvector.PGvector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PGvectorTest {
    @Test
    void testArrayConstructor() {
        PGvector vec = new PGvector(new float[] {1, 2, 3});
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGvector vec = new PGvector("[1,2,3]");
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testGetValue() throws SQLException {
        PGvector vec = new PGvector(new float[] {1, 2, 3});
        assertEquals("[1.0,2.0,3.0]", vec.getValue());
    }
}
