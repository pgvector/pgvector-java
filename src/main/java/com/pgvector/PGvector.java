package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import org.postgresql.PGConnection;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

/**
 * PGvector class
 */
public class PGvector extends PGobject implements PGBinaryObject, Serializable, Cloneable {
    private float[] vec;

    /**
     * Constructor
     */
    public PGvector() {
        type = "vector";
    }

    /**
     * Constructor
     */
    public PGvector(float[] v) {
        this();
        vec = v;
    }

    /**
     * Constructor
     */
    public PGvector(String s) throws SQLException {
        this();
        setValue(s);
    }

    /**
     * Sets the value from a text representation of a vector
     */
    public void setValue(String s) throws SQLException {
        if (s == null) {
            vec = null;
        } else {
            String[] sp = s.substring(1, s.length() - 1).split(",");
            vec = new float[sp.length];
            for (int i = 0; i < sp.length; i++) {
                vec[i] = Float.parseFloat(sp[i]);
            }
        }
    }

    /**
     * Returns the text representation of a vector
     */
    public String getValue() {
        if (vec == null) {
            return null;
        } else {
            return Arrays.toString(vec).replace(" ", "");
        }
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    public int lengthInBytes() {
        return vec == null ? 0 : 4 + vec.length * 4;
    }

    /**
     * Sets the value from a binary representation of a vector
     */
    public void setByteValue(byte[] value, int offset) throws SQLException {
        int dim = ByteConverter.int2(value, offset);

        int unused = ByteConverter.int2(value, offset + 2);
        if (unused != 0) {
            throw new SQLException("expected unused to be 0");
        }

        vec = new float[dim];
        for (int i = 0; i < dim; i++) {
            vec[i] = ByteConverter.float4(value, offset + 4 + i * 4);
        }
    }

    /**
     * Writes the binary representation of a vector
     */
    public void toBytes(byte[] bytes, int offset) {
        if (vec == null) {
            return;
        }

        // server will error on overflow due to unconsumed buffer
        // could set to Short.MAX_VALUE for friendlier error message
        ByteConverter.int2(bytes, offset, vec.length);
        ByteConverter.int2(bytes, offset + 2, 0);
        for (int i = 0; i < vec.length; i++) {
            ByteConverter.float4(bytes, offset + 4 + i * 4, vec[i]);
        }
    }

    /**
     * Returns an array
     */
    public float[] toArray() {
        return vec;
    }

    /**
     * Registers the vector type
     */
    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("vector", PGvector.class);
    }
}
