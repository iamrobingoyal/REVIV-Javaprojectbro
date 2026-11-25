// src/com/reviv/database/DatabaseInitializer.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    // SQL texts that create every table.
    private static final String[] TABLE_SQL = {
        "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, failed_attempts INT DEFAULT 0, locked BOOLEAN DEFAULT FALSE)",
        "CREATE TABLE IF NOT EXISTS sos_requests (sos_id VARCHAR(50) NOT NULL PRIMARY KEY, user_id INT, description VARCHAR(255), zone VARCHAR(50), status VARCHAR(20) NOT NULL, team_id INT DEFAULT 0, reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
        "CREATE TABLE IF NOT EXISTS rescue_teams (team_id INT AUTO_INCREMENT PRIMARY KEY, team_name VARCHAR(100) NOT NULL, captain VARCHAR(50), available BOOLEAN DEFAULT TRUE)",
        "CREATE TABLE IF NOT EXISTS resources (resource_id INT AUTO_INCREMENT PRIMARY KEY, resource_name VARCHAR(100) NOT NULL UNIQUE, quantity INT NOT NULL, used_quantity INT DEFAULT 0)",
        "CREATE TABLE IF NOT EXISTS shelters (shelter_id INT AUTO_INCREMENT PRIMARY KEY, shelter_name VARCHAR(100) NOT NULL, zone VARCHAR(50), capacity INT NOT NULL, free_capacity INT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS hospitals (hospital_id INT AUTO_INCREMENT PRIMARY KEY, hospital_name VARCHAR(100) NOT NULL, zone VARCHAR(50), free_beds INT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS volunteers (volunteer_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, skill VARCHAR(100), user_id INT DEFAULT 0, available BOOLEAN DEFAULT TRUE)",
        "CREATE TABLE IF NOT EXISTS alerts (alert_id INT AUTO_INCREMENT PRIMARY KEY, sos_id VARCHAR(50), zone VARCHAR(50), message VARCHAR(255))",
        "CREATE TABLE IF NOT EXISTS donations (donation_id INT AUTO_INCREMENT PRIMARY KEY, donor VARCHAR(100), amount DOUBLE)"
    };

    // SQL texts that insert the sample data.
    private static final String[] SAMPLE_SQL = {
        "INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN'), ('admin2', 'admin123', 'ADMIN'), ('admin3', 'admin123', 'ADMIN'), ('dispatcher', 'dispatcher123', 'DISPATCHER'), ('dispatcher2', 'dispatcher123', 'DISPATCHER'), ('dispatcher3', 'dispatcher123', 'DISPATCHER'), ('citizen', 'citizen123', 'CITIZEN'), ('citizen2', 'citizen123', 'CITIZEN'), ('citizen3', 'citizen123', 'CITIZEN'), ('volunteer', 'volunteer123', 'VOLUNTEER'), ('volunteer2', 'volunteer123', 'VOLUNTEER'), ('volunteer3', 'volunteer123', 'VOLUNTEER')",
        "INSERT INTO rescue_teams (team_name, captain, available) VALUES ('Team Alpha', 'Rahul', TRUE), ('Team Beta', 'Anita', TRUE), ('Team Gamma', 'Vikram', FALSE), ('Team Delta', 'Meena', TRUE), ('Team Echo', 'Suresh', TRUE)",
        "INSERT INTO hospitals (hospital_name, zone, free_beds) VALUES ('Hospital City', 'Zone A', 20), ('Hospital Civil', 'Zone B', 15), ('Hospital Gov', 'Zone A', 30), ('Hospital St Mary', 'Zone C', 10)",
        "INSERT INTO shelters (shelter_name, zone, capacity, free_capacity) VALUES ('Shelter Relief', 'Zone A', 50, 50), ('Shelter Safe Haven', 'Zone B', 40, 40), ('Shelter Hope Center', 'Zone C', 25, 25)",
        "INSERT INTO resources (resource_name, quantity) VALUES ('Food Packet', 100), ('Water Bottle', 200), ('Blanket', 80), ('Medical Kit', 30), ('Tent', 12)",
        "INSERT INTO volunteers (name, skill, user_id, available) VALUES ('John', 'Medical', (SELECT id FROM users WHERE username = 'volunteer'), TRUE), ('Mary', 'Medical', (SELECT id FROM users WHERE username = 'volunteer2'), TRUE), ('Ravi', 'Rescue', (SELECT id FROM users WHERE username = 'volunteer3'), TRUE), ('Kavya', 'Rescue', 0, TRUE), ('Arjun', 'Cooking', 0, TRUE)"
    };

    // Private constructor so nobody creates objects of this helper.
    private DatabaseInitializer() {
    }

    // Creates the database, the missing tables and the sample data.
    public static void initialize() throws DatabaseException {
        createDatabase();
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            runAll(stmt, TABLE_SQL);
            if (isUsersTableEmpty(stmt)) {
                runAll(stmt, SAMPLE_SQL);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database setup failed: " + e.getMessage());
        } finally {
            closeAll(null, stmt, conn);
        }
    }

    // Creates reviv_db when it does not exist yet.
    private static void createDatabase() throws DatabaseException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBConnection.getServerConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConnection.DB_NAME);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot reach MySQL: " + e.getMessage());
        } finally {
            closeAll(null, stmt, conn);
        }
    }

    // Runs every SQL text of the given array one by one.
    private static void runAll(Statement stmt, String[] sqlList) throws SQLException {
        for (int i = 0; i < sqlList.length; i++) {
            stmt.executeUpdate(sqlList[i]);
        }
    }

    // Returns true when the users table has no rows (fresh database).
    private static boolean isUsersTableEmpty(Statement stmt) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            return rs.getInt(1) == 0;
        } finally {
            closeAll(rs, null, null);
        }
    }

    // Closes result set, statement and connection safely.
    private static void closeAll(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close ResultSet");
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close Statement");
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close Connection");
        }
    }
}
