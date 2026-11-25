// src/com/reviv/database/SOSDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.SOSRequest;

public class SOSDAO {
    // Start of every SOS SELECT query.
    private static final String SOS_COLUMNS = "SELECT sos_id, user_id, description, zone, status, team_id FROM sos_requests";

    // Saves a new SOS request.
    public void insertSos(SOSRequest s) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO sos_requests (sos_id, user_id, description, zone, status, team_id) VALUES (?, ?, ?, ?, ?, 0)");
            ps.setString(1, s.getSosId());
            ps.setInt(2, s.getUserId());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getZone());
            ps.setString(5, s.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save SOS: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Returns all SOS requests, oldest first.
    public ArrayList<SOSRequest> getAllSosRequests() throws DatabaseException {
        return runQuery(SOS_COLUMNS + " ORDER BY reported_at, sos_id", "", 0);
    }

    // Returns all SOS requests that have the given status.
    public ArrayList<SOSRequest> getSosByStatus(String status) throws DatabaseException {
        return runQuery(SOS_COLUMNS + " WHERE status = ? ORDER BY reported_at, sos_id", status, 0);
    }

    // Returns all SOS requests raised by one user.
    public ArrayList<SOSRequest> getSosByUser(int userId) throws DatabaseException {
        return runQuery(SOS_COLUMNS + " WHERE user_id = ? ORDER BY reported_at, sos_id", "", userId);
    }

    // Finds one SOS by id, or returns null when not found.
    public SOSRequest getSosById(String sosId) throws DatabaseException {
        ArrayList<SOSRequest> found = runQuery(SOS_COLUMNS + " WHERE sos_id = ?", sosId, 0);
        if (found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    // Changes the status of one SOS.
    public void updateStatus(String sosId, String status) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE sos_requests SET status = ? WHERE sos_id = ?");
            ps.setString(1, status);
            ps.setString(2, sosId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update SOS status: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Saves the team of an SOS and marks it ASSIGNED.
    public void assignTeam(String sosId, int teamId, String status) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE sos_requests SET team_id = ?, status = ? WHERE sos_id = ?");
            ps.setInt(1, teamId);
            ps.setString(2, status);
            ps.setString(3, sosId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to assign team: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Runs one SOS query. Text parameter is used when not empty, number parameter when above zero.
    private ArrayList<SOSRequest> runQuery(String sql, String text, int number) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (text.length() > 0) {
                ps.setString(1, text);
            }
            if (number > 0) {
                ps.setInt(1, number);
            }
            rs = ps.executeQuery();
            return toSosList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load SOS requests: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Converts the SOS result set into a list of SOS requests.
    private ArrayList<SOSRequest> toSosList(ResultSet rs) throws SQLException {
        ArrayList<SOSRequest> list = new ArrayList<SOSRequest>();
        while (rs.next()) {
            list.add(new SOSRequest(rs.getString("sos_id"), rs.getInt("user_id"), rs.getString("description"),
                    rs.getString("zone"), rs.getString("status"), rs.getInt("team_id")));
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
