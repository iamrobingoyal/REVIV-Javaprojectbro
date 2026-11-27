// src/com/reviv/service/TeamService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.model.RescueTeam;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TeamService {
    // Resources used up by every rescue assignment.
    public static final String FOOD_PACKET = "Food Packet";
    public static final String MEDICAL_KIT = "Medical Kit";
    public static final int FOOD_PER_RESCUE = 2;
    public static final int KITS_PER_RESCUE = 1;
    // Rescue teams loaded from the database.
    private ArrayList<RescueTeam> teams = new ArrayList<RescueTeam>();

    // Reloads the teams from the database.
    public void loadTeams() throws DatabaseException {
        teams = dbGetAllTeams();
    }

    // Returns all teams (fresh from the database).
    public ArrayList<RescueTeam> getAllTeams() throws DatabaseException {
        loadTeams();
        return new ArrayList<RescueTeam>(teams);
    }

    // Finds a team by id in the loaded list, or returns null.
    private RescueTeam findTeam(int teamId) {
        for (RescueTeam t : teams) {
            if (t.getTeamId() == teamId) {
                return t;
            }
        }
        return null;
    }

    // Returns the name of a team, or a default text when unknown.
    public String getTeamName(int teamId) throws DatabaseException {
        loadTeams();
        RescueTeam team = findTeam(teamId);
        if (team == null) {
            return "Unknown team";
        }
        return team.getTeamName();
    }

    // Marks a chosen team busy and deducts rescue resources.
    public RescueTeam assignTeam(int teamId, ResourceService rs)
            throws TeamBusyException, ResourceUnavailableException, InvalidInputException, DatabaseException {
        loadTeams();
        RescueTeam team = findTeam(teamId);
        if (team == null) {
            throw new InvalidInputException("No team with ID " + teamId);
        }
        if (!team.isAvailable()) {
            throw new TeamBusyException(team.getTeamName() + " is busy right now");
        }
        rs.requireStock(FOOD_PACKET, FOOD_PER_RESCUE);
        rs.requireStock(MEDICAL_KIT, KITS_PER_RESCUE);
        setAvailable(teamId, false);
        rs.deductResource(FOOD_PACKET, FOOD_PER_RESCUE);
        rs.deductResource(MEDICAL_KIT, KITS_PER_RESCUE);
        return team;
    }

    // Adds a new rescue team.
    public void addTeam(String teamName, String captain) throws InvalidInputException, DatabaseException {
        if (teamName == null || teamName.trim().length() == 0 || captain == null || captain.trim().length() == 0) {
            throw new InvalidInputException("Team name and captain cannot be empty");
        }
        insertTeam(teamName, captain);
    }

    // Admin changes a team to available or busy.
    public void setTeamAvailability(int teamId, boolean available) throws InvalidInputException, DatabaseException {
        loadTeams();
        if (findTeam(teamId) == null) {
            throw new InvalidInputException("No team with ID " + teamId);
        }
        setAvailable(teamId, available);
    }

    // Direct JDBC operations for rescue teams.

    // Returns all rescue teams from the database.
    public ArrayList<RescueTeam> dbGetAllTeams() throws DatabaseException {
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
