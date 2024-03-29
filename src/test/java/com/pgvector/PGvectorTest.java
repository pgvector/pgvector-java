package com.pgvector;

import java.sql.SQLException;
import java.util.Arrays;
import com.pgvector.PGvector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testFloatListConstructor() {
        Float[] a = new Float[] {Float.valueOf(1), Float.valueOf(2), Float.valueOf(3)};
        PGvector vec = new PGvector(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testDoubleListConstructor() {
        Double[] a = new Double[] {Double.valueOf(1), Double.valueOf(2), Double.valueOf(3)};
        PGvector vec = new PGvector(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testGetValue() {
        PGvector vec = new PGvector(new float[] {1, 2, 3});
        assertEquals("[1.0,2.0,3.0]", vec.getValue());
    }
}
