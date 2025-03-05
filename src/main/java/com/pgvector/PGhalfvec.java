package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

/**
 * PGhalfvec class
 */
public class PGhalfvec extends PGobject implements Serializable, Cloneable {
    /*
     * Use float and text format for now since Float.float16ToFloat/floatToFloat16
     * are not available until Java 20
     */
    private float[] vec;

    /**
     * @hidden
     */
    public PGhalfvec() {
        type = "halfvec";
    }

    /**
     * Creates a half vector from an array
     *
     * @param v float array
     */
    public PGhalfvec(float[] v) {
        this();
        vec = v;
    }

    /**
     * Creates a half vector from a list
     *
     * @param <T> number
     * @param v list of numbers
     */
    public <T extends Number> PGhalfvec(List<T> v) {
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
     * Creates a half vector from a text representation
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
            vec = new float[sp.length];
            for (int i = 0; i < sp.length; i++) {
                vec[i] = Float.parseFloat(sp[i]);
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
            return Arrays.toString(vec).replace(" ", "");
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
}
