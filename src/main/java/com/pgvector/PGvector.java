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

    public PGvector() {
        type = "vector";
    }

    public PGvector(float[] v) {
        this();
        vec = v;
    }

    public PGvector(String s) throws SQLException {
        this();
        setValue(s);
    }

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

    public String getValue() {
        if (vec == null) {
            return null;
        } else {
            return Arrays.toString(vec).replace(" ", "");
        }
    }

    public float[] toArray() {
        return vec;
    }

    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("vector", PGvector.class);
    }
}
