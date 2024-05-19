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
 * PGhalfvec class
 */
public class PGhalfvec extends PGobject implements PGBinaryObject, Serializable, Cloneable {
    private short[] vec;

    /**
     * Constructor
     */
    public PGhalfvec() {
        type = "halfvec";
    }

    /**
     * Constructor
     *
     * @param v float array
     */
    public PGhalfvec(float[] v) {
        this();
        if (v == null) {
            vec = null;
        } else {
            vec = new short[v.length];
            for (int i = 0; i < v.length; i++) {
                vec[i] = Float.floatToFloat16(v[i]);
            }
        }
    }

    /**
     * Constructor
     *
     * @param <T> number
     * @param v list of numbers
     */
    public <T extends Number> PGhalfvec(List<T> v) {
        this();
        if (Objects.isNull(v)) {
            vec = null;
        } else {
            vec = new short[v.size()];
            int i = 0;
            for (T f : v) {
                vec[i++] = Float.floatToFloat16(f.floatValue());
            }
        }
    }

    /**
     * Constructor
     *
     * @param s text representation of a half vector
     * @throws SQLException exception
     */
    public PGhalfvec(String s) throws SQLException {
        this();
        setValue(s);
    }

    /**
     * Sets the value from a text representation of a half vector
     */
    public void setValue(String s) throws SQLException {
        if (s == null) {
            vec = null;
        } else {
            String[] sp = s.substring(1, s.length() - 1).split(",");
            vec = new short[sp.length];
            for (int i = 0; i < sp.length; i++) {
                vec[i] = Float.floatToFloat16(Float.parseFloat(sp[i]));
            }
        }
    }

    /**
     * Returns the text representation of a half vector
     */
    public String getValue() {
        if (vec == null) {
            return null;
        } else {
            float[] fvec = new float[vec.length];
            for (int i = 0; i < vec.length; i++) {
                fvec[i] = Float.float16ToFloat(vec[i]);
            }
            return Arrays.toString(fvec).replace(" ", "");
        }
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    public int lengthInBytes() {
        return vec == null ? 0 : 4 + vec.length * 2;
    }

    /**
     * Sets the value from a binary representation of a half vector
     */
    public void setByteValue(byte[] value, int offset) throws SQLException {
        int dim = ByteConverter.int2(value, offset);

        int unused = ByteConverter.int2(value, offset + 2);
        if (unused != 0) {
            throw new SQLException("expected unused to be 0");
        }

        vec = new short[dim];
        for (int i = 0; i < dim; i++) {
            vec[i] = ByteConverter.int2(value, offset + 4 + i * 2);
        }
    }

    /**
     * Writes the binary representation of a half vector
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
            ByteConverter.int2(bytes, offset + 4 + i * 2, vec[i]);
        }
    }

    /**
     * Returns an array
     *
     * @return an array
     */
    public float[] toArray() {
        float[] v = new float[vec.length];
        for (int i = 0; i < vec.length; i++) {
            v[i] = Float.float16ToFloat(vec[i]);
        }
        return v;
    }

    /**
     * Registers the halfvec type
     *
     * @param conn connection
     * @throws SQLException exception
     */
    public static void addHalfvecType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("halfvec", PGhalfvec.class);
    }
}
