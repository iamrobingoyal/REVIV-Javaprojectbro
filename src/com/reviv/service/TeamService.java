// src/com/reviv/service/TeamService.java
package com.reviv.service;

import com.reviv.database.TeamDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.model.RescueTeam;
import java.util.ArrayList;

public class TeamService {
    // Resources used up by every rescue assignment.
    public static final String FOOD_PACKET = "Food Packet";
    public static final String MEDICAL_KIT = "Medical Kit";
    public static final int FOOD_PER_RESCUE = 2;
    public static final int KITS_PER_RESCUE = 1;
    // Rescue teams loaded from the database.
    private ArrayList<RescueTeam> teams = new ArrayList<RescueTeam>();
    // DAO used to read and change teams.
    private TeamDAO teamDAO = new TeamDAO();

    // Reloads the teams from the database.
    public void loadTeams() throws DatabaseException {
        teams = teamDAO.getAllTeams();
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
        teamDAO.setAvailable(teamId, false);
        rs.deductResource(FOOD_PACKET, FOOD_PER_RESCUE);
        rs.deductResource(MEDICAL_KIT, KITS_PER_RESCUE);
        return team;
    }

    // Adds a new rescue team.
    public void addTeam(String teamName, String captain) throws InvalidInputException, DatabaseException {
        if (teamName == null || teamName.trim().length() == 0 || captain == null || captain.trim().length() == 0) {
            throw new InvalidInputException("Team name and captain cannot be empty");
        }
        teamDAO.insertTeam(teamName, captain);
    }

    // Admin changes a team to available or busy.
    public void setTeamAvailability(int teamId, boolean available) throws InvalidInputException, DatabaseException {
        loadTeams();
        if (findTeam(teamId) == null) {
            throw new InvalidInputException("No team with ID " + teamId);
        }
        teamDAO.setAvailable(teamId, available);
    }
}
