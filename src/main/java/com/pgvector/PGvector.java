package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
     * @hidden
     */
    public PGvector() {
        type = "vector";
    }

    /**
     * Creates a vector from an array
     *
     * @param v float array
     */
    public PGvector(float[] v) {
        this();
        vec = v;
    }

    /**
     * Creates a vector from a list
     *
     * @param <T> number
     * @param v list of numbers
     */
    public <T extends Number> PGvector(List<T> v) {
        this();
        if (Objects.isNull(v)) {
            vec = null;
        } else {
            vec = new float[v.size()];
            int i = 0;
            for (T f : v) {
                vec[i++] = f.floatValue();
            }
        }
    }

    /**
     * Creates a vector from a text representation
     *
     * @param s text representation of a vector
     * @throws SQLException exception
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
     *
     * @return an array
     */
    public float[] toArray() {
        return vec;
    }

    /**
     * Registers the vector type
     *
     * @param conn connection
     * @throws SQLException exception
     */
    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("vector", PGvector.class);
    }

    /**
     * Registers the vector, halfvec, and sparsevec types
     *
     * @param conn connection
     * @throws SQLException exception
     */
    public static void registerTypes(Connection conn) throws SQLException {
        // bit type should be registered separately
        addVectorType(conn);
        conn.unwrap(PGConnection.class).addDataType("halfvec", PGhalfvec.class);
        conn.unwrap(PGConnection.class).addDataType("sparsevec", PGsparsevec.class);
    }
}
