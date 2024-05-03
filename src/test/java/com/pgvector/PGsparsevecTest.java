package com.pgvector;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PGsparsevecTest {
    @Test
    void testEmptyVectorConstructor() {
        PGsparsevec sparsevec = new PGsparsevec(10);
        assertEquals(10, sparsevec.getDimension());
        assertTrue(sparsevec.getVector().isEmpty());
    }

    @Test
    void testNumberSparseVectorConstructor() throws SQLException {
        Map<Integer, Number> sparseMap = new HashMap<>();
        sparseMap.put(1, 2.3);
        sparseMap.put(8, 0.08);

        PGsparsevec sparsevec = new PGsparsevec(10, sparseMap);
        assertEquals(10, sparsevec.getDimension());
        Map<Integer, Float> vectorMap = sparsevec.getVector();
        sparseMap.keySet().forEach(key -> assertEquals(sparseMap.get(key).floatValue(), vectorMap.get(key)));
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGsparsevec sparsevec = new PGsparsevec("{1:2.3,8:0.08}/10");

        assertEquals(10, sparsevec.getDimension());

        Map<Integer, Float> vectorMap = sparsevec.getVector();
        Number index1value = vectorMap.get(1);
        assertEquals(Float.valueOf("2.3").floatValue(), index1value.floatValue());

        Number index8value = vectorMap.get(8);
        assertEquals(Float.valueOf("0.08").floatValue(), index8value.floatValue());

    }

}
