package com.pgvector;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.postgresql.PGConnection;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;

/**
 * PGsparsevec class
 */
public class PGsparsevec extends PGobject implements PGBinaryObject, Serializable, Cloneable {
    private int dimensions;
    private int[] indices;
    private float[] values;

    /**
     * Constructor
     */
    public PGsparsevec() {
        type = "sparsevec";
    }

    /**
     * Constructor
     *
     * @param v float array
     */
    public PGsparsevec(float[] v) {
        this();

        int nnz = 0;
        for (int i = 0; i < v.length; i++) {
            if (v[i] != 0) {
                nnz++;
            }
        }

        dimensions = v.length;
        indices = new int[nnz];
        values = new float[nnz];

        int j = 0;
        for (int i = 0; i < v.length; i++) {
            if (v[i] != 0) {
                indices[j] = i;
                values[j] = v[i];
                j++;
            }
        }
    }

    /**
     * Constructor for creating a sparse vector from a List.
     * <p>
     * Expects the list to represent the vector with 0-based indexing, as is standard in programming.
     * Each element in the list corresponds to a dimension of the vector (its index in the list).
     *
     * @param <T> number
     * @param v list of numbers
     */
    public <T extends Number> PGsparsevec(List<T> v) {
        this();
        if (Objects.isNull(v)) {
            indices = null;
        } else {
            int nnz = 0;
            for (T f : v) {
                if (f.floatValue() != 0) {
                    nnz++;
                }
            }

            dimensions = v.size();
            indices = new int[nnz];
            values = new float[nnz];

            int i = 0;
            int j = 0;
            for (T f : v) {
                float fv = f.floatValue();
                if (fv != 0) {
                    indices[j] = i;
                    values[j] = fv;
                    j++;
                }
                i++;
            }

        }
    }

    /**
     * Constructor for creating a sparse vector from a Map.
     * <p>
     * Expects the map keys (indices) to be 0-based, as typically used in programming.
     *
     * @param <T> number
     * @param map map of non-zero elements
     * @param dimensions number of dimensions
     */
    public <T extends Number> PGsparsevec(Map<Integer, T> map, int dimensions) {
        this();

        ArrayList<Map.Entry<Integer, T>> elements = new ArrayList<Map.Entry<Integer, T>>();
        if (!Objects.isNull(map)) {
            elements.addAll(map.entrySet());
        }
        elements.removeIf((e) -> e.getValue().floatValue() == 0);
        elements.sort((a, b) -> Integer.compare(a.getKey(), b.getKey()));

        int nnz = elements.size();
        indices = new int[nnz];
        values = new float[nnz];

        int i = 0;
        for (Map.Entry<Integer, T> e : elements) {
            indices[i] = e.getKey().intValue();
            values[i] = e.getValue().floatValue();
            i++;
        }

        this.dimensions = dimensions;
    }

    /**
     * Constructor for creating a sparse vector from its text representation.
     * <p>
     * The text representation uses 1-based indexing as per PostgreSQL SQL literal format.
     * For example: <code>'{1:1,3:2,5:3}/5'</code> represents a sparse vector with 5 dimensions.
     * Internally, the indices are converted to 0-based indexing.
     *
     * @param s text representation of a sparse vector
     * @throws SQLException exception
     */
    public PGsparsevec(String s) throws SQLException {
        this();
        setValue(s);
    }

    /**
     * Sets the sparse vector value from its text representation.
     * <p>
     * The text representation uses 1-based indexing as per PostgreSQL SQL literal format.
     * For example: <code>'{1:1,3:2,5:3}/5'</code> represents a sparse vector with 5 dimensions.
     * Internally, the indices are converted to 0-based indexing.
     * <p>
     * If the provided string is null, the vector's indices are set to null.
     * 
     * @param s the text representation of the sparse vector using 1-based indices
     */
    public void setValue(String s) throws SQLException {
        if (s == null) {
            indices = null;
        } else {
            String[] sp = s.split("/", 2);
            String[] elements = sp[0].substring(1, sp[0].length() - 1).split(",");

            dimensions = Integer.parseInt(sp[1]);
            indices = new int[elements.length];
            values = new float[elements.length];

            for (int i = 0; i < elements.length; i++)
            {
                String[] ep = elements[i].split(":", 2);
                indices[i] = Integer.parseInt(ep[0]) - 1;
                values[i] = Float.parseFloat(ep[1]);
            }
        }
    }

    /**
     * Returns the text representation of a sparse vector
     */
    public String getValue() {
        if (indices == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(13 + 27 * indices.length);
            sb.append('{');

            for (int i = 0; i < indices.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(indices[i] + 1);
                sb.append(':');
                sb.append(values[i]);
            }

            sb.append('}');
            sb.append('/');
            sb.append(dimensions);
            return sb.toString();
        }
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    public int lengthInBytes() {
        return indices == null ? 0 : 12 + indices.length * 4 + values.length * 4;
    }

    /**
     * Sets the value from a binary representation of a sparse vector
     */
    public void setByteValue(byte[] value, int offset) throws SQLException {
        dimensions = ByteConverter.int4(value, offset);
        int nnz = ByteConverter.int4(value, offset + 4);

        int unused = ByteConverter.int4(value, offset + 8);
        if (unused != 0) {
            throw new SQLException("expected unused to be 0");
        }

        indices = new int[nnz];
        for (int i = 0; i < nnz; i++) {
            indices[i] = ByteConverter.int4(value, offset + 12 + i * 4);
        }

        values = new float[nnz];
        for (int i = 0; i < nnz; i++) {
            values[i] = ByteConverter.float4(value, offset + 12 + nnz * 4 + i * 4);
        }
    }

    /**
     * Writes the binary representation of a sparse vector
     */
    public void toBytes(byte[] bytes, int offset) {
        if (indices == null) {
            return;
        }

        // server will error on overflow due to unconsumed buffer
        // could set to Integer.MAX_VALUE for friendlier error message
        ByteConverter.int4(bytes, offset, dimensions);
        ByteConverter.int4(bytes, offset + 4, indices.length);
        ByteConverter.int4(bytes, offset + 8, 0);
        for (int i = 0; i < indices.length; i++) {
            ByteConverter.int4(bytes, offset + 12 + i * 4, indices[i]);
        }
        for (int i = 0; i < values.length; i++) {
            ByteConverter.float4(bytes, offset + 12 + indices.length * 4 + i * 4, values[i]);
        }
    }

    /**
     * Returns an array
     *
     * @return an array
     */
    public float[] toArray() {
        if (indices == null) {
            return null;
        }

        float[] vec = new float[dimensions];
        for (int i = 0; i < indices.length; i++) {
            vec[indices[i]] = values[i];
        }
        return vec;
    }

    /**
     * Returns the number of dimensions
     *
     * @return the number of dimensions
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * Returns the non-zero indices
     *
     * @return the non-zero indices
     */
    public int[] getIndices() {
        return indices;
    }

    /**
     * Returns the non-zero values
     *
     * @return the non-zero values
     */
    public float[] getValues() {
        return values;
    }
}
