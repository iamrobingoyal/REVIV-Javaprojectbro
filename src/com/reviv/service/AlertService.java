// src/com/reviv/service/AlertService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Alert;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

public class AlertService {
    // Number of alerts kept in the recent list.
    private static final int MAX_ALERTS = 10;
    // Sos id used for alerts that are not linked to one SOS.
    public static final String BROADCAST_ID = "BROADCAST";
    // Last 10 alerts: addLast adds, removeFirst drops the oldest.
    private LinkedList<Alert> recentAlerts = new LinkedList<Alert>();

    // Saves a new alert in the database and in the recent list.
    public void createAlert(String sosId, String zone, String message)
            throws InvalidInputException, DatabaseException {
        if (zone == null || zone.trim().length() == 0 || message == null || message.trim().length() == 0) {
            throw new InvalidInputException("Zone and message cannot be empty");
        }
        insertAlert(sosId, zone, message);
        addToRecent(new Alert(0, sosId, zone, message));
    }

    // Adds an alert to the end of the list and drops the oldest if there are more than 10.
    private void addToRecent(Alert a) {
        recentAlerts.addLast(a);
        if (recentAlerts.size() > MAX_ALERTS) {
            recentAlerts.removeFirst();
        }
    }

    // Reloads the recent list from the database.
    public void loadAlerts() throws DatabaseException {
        recentAlerts.clear();
        ArrayList<Alert> all = getAllAlerts();
        for (Alert a : all) {
            addToRecent(a);
        }
    }

    // Returns the last 10 alerts (fresh from the database).
    public ArrayList<Alert> getRecentAlerts() throws DatabaseException {
        loadAlerts();
        return new ArrayList<Alert>(recentAlerts);
    }

    // Direct JDBC operations for alerts.

    // Returns all alerts, oldest first.
    public ArrayList<Alert> getAllAlerts() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT alert_id, sos_id, zone, message FROM alerts ORDER BY alert_id");
            rs = ps.executeQuery();
            return toAlertList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load alerts: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves a new alert.
    public void insertAlert(String sosId, String zone, String message) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO alerts (sos_id, zone, message) VALUES (?, ?, ?)");
            ps.setString(1, sosId);
            ps.setString(2, zone);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save alert: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the alert result set into a list of alerts.
    private ArrayList<Alert> toAlertList(ResultSet rs) throws SQLException {
        ArrayList<Alert> list = new ArrayList<Alert>();
        while (rs.next()) {
            list.add(new Alert(rs.getInt("alert_id"), rs.getString("sos_id"), rs.getString("zone"), rs.getString("message")));
        }
        return list;
    }

    // Closes result set, statement and connection safely.
    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close ResultSet");
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close PreparedStatement");
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
