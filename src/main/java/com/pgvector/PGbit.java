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
 * PGbit class
 */
public class PGbit extends PGobject implements PGBinaryObject, Serializable, Cloneable {
    private int length;
    private byte[] data;

    /**
     * Constructor
     */
    public PGbit() {
        type = "bit";
    }

    /**
     * Constructor
     *
     * @param v boolean array
     */
    public PGbit(boolean[] v) {
        this();
        length = v.length;
        data = new byte[(length + 7) / 8];
        for (int i = 0; i < length; i++) {
            data[i / 8] |= (v[i] ? 1 : 0) << (7 - (i % 8));
        }
    }

    /**
     * Constructor
     *
     * @param s text representation of a bit string
     * @throws SQLException exception
     */
    public PGbit(String s) throws SQLException {
        this();
        setValue(s);
    }

    /**
     * Sets the value from a text representation of a bit string
     */
    public void setValue(String s) throws SQLException {
        if (s == null) {
            data = null;
        } else {
            length = s.length();
            data = new byte[(length + 7) / 8];
            for (int i = 0; i < length; i++) {
                data[i / 8] |= (s.charAt(i) != '0' ? 1 : 0) << (7 - (i % 8));
            }
        }
    }

    /**
     * Returns the text representation of a bit string
     */
    public String getValue() {
        if (data == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(((data[i / 8] >> (7 - (i % 8))) & 1) == 1 ? '1' : '0');
            }
            return sb.toString();
        }
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    public int lengthInBytes() {
        return data == null ? 0 : 4 + data.length;
    }

    /**
     * Sets the value from a binary representation of a bit string
     */
    public void setByteValue(byte[] value, int offset) throws SQLException {
        length = ByteConverter.int4(value, offset);
        data = new byte[(length + 7) / 8];
        for (int i = 0; i < data.length; i++) {
            data[i] = value[offset + 4 + i];
        }
    }

    /**
     * Writes the binary representation of a bit string
     */
    public void toBytes(byte[] bytes, int offset) {
        if (data == null) {
            return;
        }

        ByteConverter.int4(bytes, offset, length);
        for (int i = 0; i < data.length; i++) {
            bytes[offset + 4 + i] = data[i];
        }
    }

    /**
     * Returns an array
     *
     * @return an array
     */
    public boolean[] toArray() {
        boolean[] bits = new boolean[length];
        for (int i = 0; i < length; i++) {
            bits[i] = ((data[i / 8] >> (7 - (i % 8))) & 1) == 1;
        }
        return bits;
    }

    /**
     * Registers the bit type
     *
     * @param conn connection
     * @throws SQLException exception
     */
    public static void addBitType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType("bit", PGbit.class);
    }
}
