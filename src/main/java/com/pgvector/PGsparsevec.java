package com.pgvector;

import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * PGsparsevec class
 */
public class PGsparsevec extends PGobject implements Serializable, Cloneable {

    private int dimension = -1;
    private Map<Integer, Float> sparsevecMap;
    private static final String TYPE = "sparsevec";

    /**
     * Constructor
     */
    public PGsparsevec() {
        setType(TYPE);
    }

    /**
     * Constructor
     *
     * @param dimension dimension of sparse vector
     */
    public PGsparsevec(int dimension) {
        setType(TYPE);

        if ( dimension < 1 ) {
            throw new IllegalStateException("Dimension must be greater than 0.");
        }

        this.dimension = dimension;
        this.sparsevecMap = new TreeMap<>();
    }

    /**
     * Constructor
     *
     * @param dimension dimension of sparse vector
     * @param sparsevecMap map of sparse vector
     */
    public <T extends Number> PGsparsevec(int dimension, Map<Integer, T> sparsevecMap) {
        setType(TYPE);

        if ( dimension < 1 ) {
            throw new IllegalStateException("Dimension must be greater than 0.");
        }

        this.dimension = dimension;
        this.sparsevecMap = new TreeMap<>();

        if ( sparsevecMap != null ) {
            sparsevecMap.keySet().forEach(eachKey -> this.sparsevecMap.put(eachKey, (sparsevecMap.get(eachKey).floatValue())));
        }
    }

    /**
     * Constructor
     *
     * @param value text representation of a sparse vector
     * @throws SQLException exception
     */
    public PGsparsevec(String value) throws SQLException {
        setType(TYPE);
        setValue(value);
    }

    /**
     * Return the dimension size of the sparse vector.
     * @return dimension
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Return the sparse vector map
     * @return vector map
     */
    public Map<Integer, Float> getVector() {
        return sparsevecMap;
    }

    /**
     * Set the value at the offset of the sparse vector.
     * @param offset offset of sparse vector
     * @param value value
     */
    public void putValue(int offset, Number value) {
        if ( value == null ) {
            throw new IllegalStateException("number must not be null.");
        }

        this.putValue(offset, value.floatValue());
    }

    /**
     * Set the value at the offset of the sparse vector.
     * @param offset offset of sparse vector
     * @param value value
     */
    public void putValue(int offset, float value) {
        if ( dimension == -1 ) {
            throw new IllegalStateException("Dimension has not been initialized.");
        }

        if ( dimension <= offset ) {
            throw new IllegalStateException("offset must be less than the size of the dimension.");
        }

        if ( sparsevecMap == null ) {
            sparsevecMap = new TreeMap<>();
        }

        sparsevecMap.put(offset, value);
    }

    /**
     * Sets the value from a text representation of a sparse vector
     */
    public void setValue(String value) throws SQLException {
        if (value == null) {
            this.dimension = -1;
            this.sparsevecMap = null;
        } else {
            TreeMap<Integer, Float> tempSparsevecMap = new TreeMap<>();
            int tempDimension = -1;

            String[] parts = value.split("/"); // {1:0.87,...}/10
            try {
                tempDimension = Integer.parseInt(parts[1]); // 10
            } catch ( NumberFormatException nfe ) {
                throw new SQLException("The dimension part of the sparsevec is invalid.", nfe);
            }

            try {
                String sparseVectorParts = parts[0].substring(1, parts[0].length() - 1); // {1:0.87,...}
                String[] sparseVectorPartArray = sparseVectorParts.split(",");

                for (String eachVectorPart : sparseVectorPartArray) {
                    String[] offsetValuePart = eachVectorPart.split(":");
                    int offset = Integer.parseInt(offsetValuePart[0]);
                    float floatValue = Float.parseFloat(offsetValuePart[1]);
                    tempSparsevecMap.put(offset, floatValue);
                }
            } catch (Exception exception) {
                throw new SQLException("The vector part of the sparsevec is invalid.", exception);
            }

            this.dimension = tempDimension;
            this.sparsevecMap = tempSparsevecMap;
        }
    }

    /**
     * Returns the text representation of a sparse vector
     */
    public String getValue() {
        if ( this.sparsevecMap == null ) {
            return null;
        }

        if ( sparsevecMap.isEmpty() ) {
            return "{}/" + dimension;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String strVectorMapPart = sparsevecMap.keySet().stream().map( key -> key + ":" + sparsevecMap.get(key)).collect(Collectors.joining(","));
        sb.append(strVectorMapPart);
        sb.append("}/").append(dimension);

        return sb.toString();
    }

    /**
     * Registers the vector type
     *
     * @param conn connection
     * @throws SQLException exception
     */
    public static void addVectorType(Connection conn) throws SQLException {
        conn.unwrap(PGConnection.class).addDataType(TYPE, PGsparsevec.class);
    }
}
