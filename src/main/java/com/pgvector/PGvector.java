package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.N;
import org.postgresql.PGConnection;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

/**
 * PGvector class
 */
public class PGvector extends PGobject implements PGBinaryObject, Serializable, Cloneable {

    private static final int HEADER_SIZE = 4;
    private float @Nullable [] vec;

    /**
     * Constructor
     */
    public PGvector() {
        type = "vector";
    }

    /**
     * Constructor
     */
    public PGvector(@Nullable float[] v) {
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
     * Sets the value from an array of bytes,
     * This is handles the output of vector_send in vector.c
     * @param value
     * @param offset
     * @throws SQLException
     */
    public void setByteValue(byte[] value, int offset) throws SQLException {
        /*
        vector_send sends a 2 byte integer representing the length of the array
         */
        final int floatArraySize = ByteConverter.int2(value,0);

        if (vec == null ) {
            vec = new float[floatArraySize];
        } else if ( offset == 0 ) {
            if (vec.length < floatArraySize ) {
                // extend the vector array
                vec = Arrays.copyOf(vec, floatArraySize);
            }
        } else {
            if (offset + floatArraySize > vec.length) {
                // extend the array
                vec = Arrays.copyOf(vec, offset + floatArraySize);
            }
        }
        // copy the incoming data into the array,
        for (int i=0; i< floatArraySize; i++){
            /*
             floats are 4 bytes wide and we start 4 bytes in to account
             for the length and 2 bytes which are ignored see vector.c
             */
            vec[offset++] = ByteConverter.float4(value, i*4 + 4);
        }
    }

    public int lengthInBytes() {
        if (isNull()) {
            return 0;
        } else {
            return vec.length * 4 + HEADER_SIZE;
        }
    }

    /**
     * Send vector back to server in binary see vector_recv in vector.c
     * @param bytes destination of byte representation of vector
     * @param offset
     */
    @Override
    public void toBytes(byte[] bytes, int offset) {
        if (isNull()) {
            return;
        }
        int pos=0;

        // set the number of dimensions
        ByteConverter.int2(bytes,pos,vec.length);
        // set the flags unused
        pos=2;
        ByteConverter.int2(bytes, pos, 0);
        // set the oid
        pos=4;
        for (int index = 0 ; index < vec.length ; index++ ) {
            ByteConverter.float4(bytes,pos, vec[index] );
            pos += 4;
        }
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
     * Returns an array
     */
    public float[] toArray() {
        return vec;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null && vec == null ) {
            return true;
        }

        if ( ! (obj instanceof  float[]) ) {
            return false;
        }
        if ( ((float[]) obj).length != vec.length ) {
            return false;
        }
        for ( int i=0; i< vec.length; i++){
            if (((float[]) obj)[i] != vec[i]) {
                return false;
            }
        }
        return true;
    }
    /**
     * Registers the vector type
     */
    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("vector", PGvector.class);
    }
}
