package com.pgvector;

import java.sql.SQLException;
import java.util.Arrays;
import com.pgvector.PGbit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PGbitTest {
    @Test
    void testArrayConstructor() {
        PGbit vec = new PGbit(new boolean[] {false, true, false, true, false, false, false, false, true});
        assertEquals(9, vec.length());
        assertArrayEquals(new byte[] {(byte) 0b01010000, (byte) 0b10000000}, vec.toByteArray());
        assertArrayEquals(new boolean[] {false, true, false, true, false, false, false, false, true}, vec.toArray());
    }

    void testEmptyArrayConstructor() {
        PGbit vec = new PGbit(new boolean[] {});
        assertEquals(0, vec.length());
        assertArrayEquals(new byte[] {}, vec.toByteArray());
        assertArrayEquals(new boolean[] {}, vec.toArray());
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGbit vec = new PGbit("010100001");
        assertEquals(9, vec.length());
        assertArrayEquals(new byte[] {(byte) 0b01010000, (byte) 0b10000000}, vec.toByteArray());
        assertArrayEquals(new boolean[] {false, true, false, true, false, false, false, false, true}, vec.toArray());
    }

    @Test
    void testGetValue() {
        PGbit vec = new PGbit(new boolean[] {false, true, false, true, false, false, false, false, true});
        assertEquals("010100001", vec.getValue());
    }
}
