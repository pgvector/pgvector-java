package com.pgvector;

import org.postgresql.PGConnection;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PGsparsevec extends PGobject implements Serializable, Cloneable {

    private int dimension = -1;
    private Map<Integer, Float> sparsevecMap;
    private static final String TYPE = "sparsevec";

    public PGsparsevec() {
        setType(TYPE);
    }

    public PGsparsevec(int dimension) {
        setType(TYPE);

        if ( dimension < 1 ) {
            throw new IllegalStateException("Dimension must be greater than 0.");
        }

        this.dimension = dimension;
        this.sparsevecMap = new TreeMap<>();
    }

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

    public PGsparsevec(String value) throws SQLException {
        setType(TYPE);
        setValue(value);
    }

    public int getDimension() {
        return dimension;
    }

    public Map<Integer, Float> getVector() {
        return sparsevecMap;
    }

    public void putValue(int index, Number number) {
        if ( number == null ) {
            throw new IllegalStateException("number must not be null.");
        }

        this.putValue(index, number.floatValue());
    }

    public void putValue(int index, float value) {
        if ( dimension == -1 ) {
            throw new IllegalStateException("Dimension has not been initialized.");
        }

        if ( dimension <= index ) {
            throw new IllegalStateException("Index must be less than the size of the dimension.");
        }

        if ( sparsevecMap == null ) {
            sparsevecMap = new TreeMap<>();
        }

        sparsevecMap.put(index, value);
    }


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
                    String[] indexValuePart = eachVectorPart.split(":");
                    int index = Integer.parseInt(indexValuePart[0]);
                    float floatValue = Float.parseFloat(indexValuePart[1]);
                    tempSparsevecMap.put(index, floatValue);
                }
            } catch (Exception exception) {
                throw new SQLException("The vector part of the sparsevec is invalid.", exception);
            }

            this.dimension = tempDimension;
            this.sparsevecMap = tempSparsevecMap;
        }
    }

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

    public static void addVectorType(Connection connection) throws SQLException {
        ((PGConnection)connection.unwrap(PGConnection.class)).addDataType(TYPE, PGsparsevec.class);
    }
}
