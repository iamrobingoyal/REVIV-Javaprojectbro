// src/com/reviv/database/AlertDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.Alert;

public class AlertDAO {
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
