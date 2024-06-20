package com.pgvector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.pgvector.PGsparsevec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PGsparsevecTest {
    @Test
    void testArrayConstructor() {
        PGsparsevec vec = new PGsparsevec(new float[] {1, 0, 2, 0, 3, 0});
        assertArrayEquals(new float[] {1, 0, 2, 0, 3, 0}, vec.toArray());
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGsparsevec vec = new PGsparsevec("{1:1,3:2,5:3}/6");
        assertArrayEquals(new float[] {1, 0, 2, 0, 3, 0}, vec.toArray());
    }

    @Test
    void testFloatListConstructor() {
        Float[] a = new Float[] {Float.valueOf(1), Float.valueOf(2), Float.valueOf(3)};
        PGsparsevec vec = new PGsparsevec(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testDoubleListConstructor() {
        Double[] a = new Double[] {Double.valueOf(1), Double.valueOf(2), Double.valueOf(3)};
        PGsparsevec vec = new PGsparsevec(Arrays.asList(a));
        assertArrayEquals(new float[] {1, 2, 3}, vec.toArray());
    }

    @Test
    void testMapConstructor() {
        Map<Integer, Float> map = new HashMap<Integer, Float>();
        map.put(Integer.valueOf(2), Float.valueOf(2));
        map.put(Integer.valueOf(4), Float.valueOf(3));
        map.put(Integer.valueOf(0), Float.valueOf(1));
        map.put(Integer.valueOf(3), Float.valueOf(0));
        PGsparsevec vec = new PGsparsevec(map, 6);
        assertArrayEquals(new float[] {1, 0, 2, 0, 3, 0}, vec.toArray());
        assertArrayEquals(new int[] {0, 2, 4}, vec.getIndices());
    }

    @Test
    void testGetValue() {
        PGsparsevec vec = new PGsparsevec(new float[] {1, 0, 2, 0, 3, 0});
        assertEquals("{1:1.0,3:2.0,5:3.0}/6", vec.getValue());
    }

    @Test
    void testGetDimensions() {
        PGsparsevec vec = new PGsparsevec(new float[] {1, 0, 2, 0, 3, 0});
        assertEquals(6, vec.getDimensions());
    }

    @Test
    void testGetIndices() {
        PGsparsevec vec = new PGsparsevec(new float[] {1, 0, 2, 0, 3, 0});
        assertArrayEquals(new int[] {0, 2, 4}, vec.getIndices());
    }

    @Test
    void testGetValues() {
        PGsparsevec vec = new PGsparsevec(new float[] {1, 0, 2, 0, 3, 0});
        assertArrayEquals(new float[] {1, 2, 3}, vec.getValues());
    }
}
