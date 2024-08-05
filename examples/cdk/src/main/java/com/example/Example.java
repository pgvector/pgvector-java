package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.pgvector.PGbit;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.CircularFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class Example {
    public static void main(String[] args) throws CDKException, InvalidSmilesException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pgvector_example");

        Statement setupStmt = conn.createStatement();
        setupStmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
        setupStmt.executeUpdate("DROP TABLE IF EXISTS molecules");

        Statement createStmt = conn.createStatement();
        createStmt.executeUpdate("CREATE TABLE molecules (id text PRIMARY KEY, fingerprint bit(2048))");

        String[] molecules = {
            "Cc1ccccc1",
            "Cc1ncccc1",
            "c1ccccn1"
        };
        for (String molecule : molecules) {
            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO molecules (id, fingerprint) VALUES (?, ?)");
            insertStmt.setString(1, molecule);
            insertStmt.setObject(2, new PGbit(generateFingerprint(molecule)));
            insertStmt.executeUpdate();
        }

        String queryMolecule = "c1ccco1";
        PreparedStatement queryStmt = conn.prepareStatement("SELECT id, fingerprint <%> ? AS distance FROM molecules ORDER BY distance LIMIT 5");
        queryStmt.setObject(1, new PGbit(generateFingerprint(queryMolecule)));
        ResultSet rs = queryStmt.executeQuery();
        while (rs.next()) {
            System.out.println(String.format("%s: %f", rs.getString("id"), rs.getDouble("distance")));
        }

        conn.close();
    }

    private static boolean[] generateFingerprint(String molecule) throws CDKException, InvalidSmilesException {
        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer m = sp.parseSmiles(molecule);

        CircularFingerprinter fingerprinter = new CircularFingerprinter(CircularFingerprinter.CLASS_ECFP6, 2048);
        IBitFingerprint fp = fingerprinter.getBitFingerprint(m);

        boolean[] ba = new boolean[(int) fp.size()];
        for (int i : fp.getSetbits()) {
            ba[i] = true;
        }
        return ba;
    }
}
