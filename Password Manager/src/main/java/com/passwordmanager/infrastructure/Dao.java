package com.passwordmanager.infrastructure;

import com.passwordmanager.api.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.passwordmanager.security.AES;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Dao class is a Data Object Access class where the database handling is done.
 */
public class Dao {

    // The SQLite JDBC URL
    private static final String JDBC_SQLITE_URL = "jdbc:sqlite:OB.db";

    // Logger for the Dao (Data Access Object) class
    private static final Logger LOG = LoggerFactory.getLogger(Dao.class);

    /**
     * init() method initializes the database.
     */
    public void init() throws SQLException {
        LOG.info("Initializing db");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            LOG.info("Connection to SQLite has been established.");
        } catch (SQLException e) {
            LOG.warn("Got exception: ", e);
            throw e;
        } finally {
            closeConnection(conn, null, null);
        }
    }

    /**
     * initTables() method initializes the database tables(table in this case - the passwords table).
     */
    public void initTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS passwords ( id text PRIMARY KEY NOT NULL, date text, key text, value text);";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            LOG.warn("Got exception: ", e);
            throw e;
        } finally {
            closeConnection(conn, stmt, null);
        }

    }

    /**
     * insert() method which takes all the necessary fields to create a password in the "passwords" db table
     */
    public void insert(String id, LocalDate date, String key, String value) throws SQLException {
        String sql = "INSERT INTO passwords(id, date, key, value) VALUES(?,?,?,?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, date.toString());
            pstmt.setString(3, key);
            pstmt.setString(4, value);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOG.warn("Got exception: ", e);
            throw e;
        } finally {
            closeConnection(conn, pstmt, null);
        }
    }

    /**
     * deleteOnId() method deletes a password from the database based on the ID of the password.
     */
    public void deleteOnId(String id) throws SQLException {
        String sql = "DELETE FROM passwords WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.warn("Got exception: ", e);
        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    /**
     * selectAll method selects all the passwords from the "passwords" table in the database and returns them.
     */
    public List<Password> selectAll() throws SQLException {
        String sql = "SELECT * FROM passwords";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            pstmt = conn.prepareStatement(sql);
            return getPasswords(pstmt, rs);
        } catch (SQLException e) {
            LOG.warn("Got exception: ", e);
            return Collections.emptyList();
        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    /**
     * selectOnId() method selects a password from the database based on the ID of the password and returns it.
     */
    public List<Password> selectOnId(String id) throws SQLException {
        String sql = "SELECT * FROM passwords WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(JDBC_SQLITE_URL);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            return getPasswords(pstmt, rs);
        } catch (SQLException e) {
            LOG.warn("Got exception: ", e);
            return Collections.emptyList();
        } finally {
            closeConnection(conn, pstmt, rs);
        }
    }

    /**
     * getPasswords method that takes a PreparedStatement and a ResultSet as parameters.
     * Gets the passwords and stores them in a list. Returns the list of passwords.
     */
    private List<Password> getPasswords(PreparedStatement pstmt, ResultSet rs) throws SQLException {
        List<Password> passwords = new ArrayList<>();

        for (rs = pstmt.executeQuery(); rs.next();) {
            Password comp = getPasswords(rs);
            passwords.add(comp);
        }
        return passwords;
    }

    /**
     * getPasswords method that takes a ResultSet as parameter.
     * Gets each value field of the password and creates a new Password object with these values.
     * Decrypts the values with AES decrypt() method and then returns the password object.
     */
    private Password getPasswords(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String date = rs.getString("date");
        String key = rs.getString("key");
        String value = rs.getString("value");

        Password comp = new Password();
        comp.setId(id);
        comp.setDate(date);
        comp.setKey(AES.decrypt(key, "karim"));
        comp.setValue(AES.decrypt(value, "karim"));

        return comp;
    }

    /**
     * closeConnection method closes the connection, the statement and the resultset.
     */
    private void closeConnection(Connection conn, Statement stmnt, ResultSet rs) throws SQLException {
        try {
            if (conn != null) {
                conn.close();
            }

            if (stmnt != null) {
                stmnt.close();
            }

            if (rs != null) {
                rs.close();
            }

        } catch (SQLException ex) {
            LOG.warn("Got exception when closing connection to db: ", ex);
            throw ex;
        }
    }

}
