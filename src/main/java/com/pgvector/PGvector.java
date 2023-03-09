package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

/**
 * PGvector class
 */
public class PGvector extends PGobject implements Serializable, Cloneable {
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
     * Sets the value from the text representation of a vector
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
