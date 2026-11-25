// src/com/reviv/database/DBConnection.java
package com.reviv.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    // MySQL server address without a database name.
    public static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    // Name of the project database.
    public static final String DB_NAME = "reviv_db";
    // JDBC URL of the reviv_db database.
    public static final String URL = SERVER_URL + DB_NAME;
    // MySQL user name (change to your own).
    public static final String USER = "root";
    // MySQL password (change to your own).
    public static final String PASSWORD = "0000";

    // Private constructor so nobody creates objects of this helper.
    private DBConnection() {
    }

    // Opens a new connection to reviv_db. The caller must close it.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Opens a connection to the MySQL server only (used to create reviv_db).
    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
    }
}
