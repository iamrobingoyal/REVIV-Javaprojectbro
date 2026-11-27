// src/com/reviv/service/ReportService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.model.RescueTeam;
import com.reviv.model.Resource;
import com.reviv.model.SOSRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportService {
    // Reuse the application services for report data.
    private SOSService sosService = new SOSService(new ZoneService());
    private TeamService teamService = new TeamService();
    private ResourceService resourceService = new ResourceService();

    // Counts incidents (SOS requests) per zone.
    public HashMap<String, Integer> getIncidentsPerZone() throws DatabaseException {
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        for (SOSRequest s : sosService.getAllSosRequests()) {
            addOne(counts, s.getZone());
        }
        return counts;
    }

    // Counts how many SOS requests each team was given.
    public HashMap<String, Integer> getTeamWorkload() throws DatabaseException {
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        ArrayList<RescueTeam> teams = teamService.getAllTeams();
        for (SOSRequest s : sosService.getAllSosRequests()) {
            if (s.getTeamId() > 0) {
                addOne(counts, findTeamName(teams, s.getTeamId()));
            }
        }
        return counts;
    }

    // Returns all resources with their used quantity.
    public ArrayList<Resource> getResourceUsage() throws DatabaseException {
        return resourceService.getAllResources();
    }

    // Adds 1 to the count of a key in the map.
    private void addOne(HashMap<String, Integer> counts, String key) {
        Integer old = counts.get(key);
        if (old == null) {
            counts.put(key, 1);
        } else {
            counts.put(key, old.intValue() + 1);
        }
    }

    // Finds the team name for an id.
    private String findTeamName(ArrayList<RescueTeam> teams, int teamId) {
        for (RescueTeam t : teams) {
            if (t.getTeamId() == teamId) {
                return t.getTeamName();
            }
        }
        return "Team " + teamId;
    }
}
