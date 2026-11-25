// src/com/reviv/database/TeamDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.RescueTeam;

public class TeamDAO {
    // Returns all rescue teams from the database.
    public ArrayList<RescueTeam> getAllTeams() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT team_id, team_name, captain, available FROM rescue_teams ORDER BY team_id");
            rs = ps.executeQuery();
            return toTeamList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load rescue teams: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves a new rescue team (available by default).
    public void insertTeam(String teamName, String captain) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO rescue_teams (team_name, captain, available) VALUES (?, ?, TRUE)");
            ps.setString(1, teamName);
            ps.setString(2, captain);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add team: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Marks a team available or busy.
    public void setAvailable(int teamId, boolean available) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE rescue_teams SET available = ? WHERE team_id = ?");
            ps.setBoolean(1, available);
            ps.setInt(2, teamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update team: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the team result set into a list of rescue teams.
    private ArrayList<RescueTeam> toTeamList(ResultSet rs) throws SQLException {
        ArrayList<RescueTeam> list = new ArrayList<RescueTeam>();
        while (rs.next()) {
            list.add(new RescueTeam(rs.getInt("team_id"), rs.getString("team_name"),
                    rs.getString("captain"), rs.getBoolean("available")));
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
