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
        PGbit vec = new PGbit(new boolean[] {true, false, true});
        assertEquals("101", vec.getValue());
    }

    @Test
    void testStringConstructor() throws SQLException {
        PGbit vec = new PGbit("101");
        assertEquals("101", vec.getValue());
    }
}
