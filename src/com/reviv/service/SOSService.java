// src/com/reviv/service/SOSService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.DuplicateSOSException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.model.RescueTeam;
import com.reviv.model.SOSRequest;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class SOSService {
    // The four allowed SOS statuses.
    public static final String REPORTED = "REPORTED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RESOLVED = "RESOLVED";
    // Pending SOS queue: addLast to add, removeFirst to process (first come, first served).
    private LinkedList<SOSRequest> pendingQueue = new LinkedList<SOSRequest>();
    // Every SOS id used so far (rejects duplicates).
    private HashSet<String> seenSosIds = new HashSet<String>();
    // Keeps the set of affected zones.
    private ZoneService zoneService;

    // Creates the service with the shared zone service.
    public SOSService(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    // Loads old SOS requests from the database at startup.
    public void loadFromDatabase() throws DatabaseException {
        ArrayList<SOSRequest> all = getAllSosRequests();
        for (SOSRequest s : all) {
            seenSosIds.add(s.getSosId());
            zoneService.addZone(s.getZone());
            if (s.getStatus().equals(REPORTED)) {
                pendingQueue.addLast(s);
            }
        }
    }

    // Saves a new SOS and adds it to the pending queue.
    public void addSosRequest(SOSRequest s) throws InvalidInputException, DuplicateSOSException, DatabaseException {
        if (s.getSosId().length() == 0 || s.getZone().length() == 0 || s.getDescription().length() == 0) {
            throw new InvalidInputException("SOS ID, zone and description cannot be empty");
        }
        if (isDuplicate(s.getSosId())) {
            throw new DuplicateSOSException("SOS ID " + s.getSosId() + " already exists");
        }
        insertSos(s);
        seenSosIds.add(s.getSosId());
        zoneService.addZone(s.getZone());
        pendingQueue.addLast(s);
    }

    // Returns true when the SOS id was already used.
    public boolean isDuplicate(String sosId) {
        return seenSosIds.contains(sosId);
    }

    // Returns the pending queue as a list (oldest first).
    public ArrayList<SOSRequest> getPendingQueue() {
        return new ArrayList<SOSRequest>(pendingQueue);
    }

    // Returns the first pending SOS without removing it, or null.
    public SOSRequest peekNextSos() {
        if (pendingQueue.isEmpty()) {
            return null;
        }
        return pendingQueue.getFirst();
    }

    // Assigns the chosen team to the first SOS. Queue changes only if everything worked.
    public SOSRequest processNextSos(int teamId, TeamService ts, ResourceService rs)
            throws TeamBusyException, ResourceUnavailableException, InvalidInputException, DatabaseException {
        SOSRequest next = peekNextSos();
        if (next == null) {
            throw new InvalidInputException("There is no pending SOS request");
        }
        RescueTeam team = ts.assignTeam(teamId, rs);
        assignTeam(next.getSosId(), team.getTeamId(), ASSIGNED);
        pendingQueue.removeFirst();
        next.setStatus(ASSIGNED);
        next.setTeamId(team.getTeamId());
        return next;
    }

    // Changes the status of an SOS. Only the next step in the chain is allowed.
    public void changeStatus(String sosId, String newStatus) throws InvalidInputException, DatabaseException {
        SOSRequest s = getSosById(sosId);
        if (s == null) {
            throw new InvalidInputException("No SOS with ID " + sosId);
        }
        if (!isValidJump(s.getStatus(), newStatus)) {
            throw new InvalidInputException("Invalid status change: " + s.getStatus() + " to " + newStatus);
        }
        updateStatus(sosId, newStatus);
    }

    // Returns true only for REPORTED-ASSIGNED, ASSIGNED-IN_PROGRESS, IN_PROGRESS-RESOLVED.
    private boolean isValidJump(String oldStatus, String newStatus) {
        if (oldStatus.equals(REPORTED)) {
            return newStatus.equals(ASSIGNED);
        }
        if (oldStatus.equals(ASSIGNED)) {
            return newStatus.equals(IN_PROGRESS);
        }
        if (oldStatus.equals(IN_PROGRESS)) {
            return newStatus.equals(RESOLVED);
        }
        return false;
    }

    // Returns SOS requests that have a team and wait for the rescue to start.
    public ArrayList<SOSRequest> getAssignedRequests() throws DatabaseException {
        return getSosByStatus(ASSIGNED);
    }

    // Returns the SOS requests raised by one user.
    public ArrayList<SOSRequest> getSosForUser(int userId) throws DatabaseException {
        return getSosByUser(userId);
    }

    // Direct JDBC operations for SOS requests.

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
