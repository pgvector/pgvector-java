package com.pgvector;

import java.sql.SQLException;
import java.util.Arrays;
import com.pgvector.PGhalfvec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PGhalfvecTest {
    @Test
    void testArrayConstructor() {
        PGhalfvec vec = new PGhalfvec(new float[] {1, 2, 3});
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGhalfvec vec = new PGhalfvec("[1,2,3]");
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testFloatListConstructor() {
        Float[] a = new Float[] {Float.valueOf(1), Float.valueOf(2), Float.valueOf(3)};
        PGhalfvec vec = new PGhalfvec(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testDoubleListConstructor() {
        Double[] a = new Double[] {Double.valueOf(1), Double.valueOf(2), Double.valueOf(3)};
        PGhalfvec vec = new PGhalfvec(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testGetValue() {
        PGhalfvec vec = new PGhalfvec(new float[] {1, 2, 3});
        assertEquals("[1.0,2.0,3.0]", vec.getValue());
    }
}
