// src/com/reviv/service/SOSService.java
package com.reviv.service;

import com.reviv.database.SOSDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.DuplicateSOSException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.model.RescueTeam;
import com.reviv.model.SOSRequest;
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
    // DAO used to save and load SOS requests.
    private SOSDAO sosDAO = new SOSDAO();
    // Keeps the set of affected zones.
    private ZoneService zoneService;

    // Creates the service with the shared zone service.
    public SOSService(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    // Loads old SOS requests from the database at startup.
    public void loadFromDatabase() throws DatabaseException {
        ArrayList<SOSRequest> all = sosDAO.getAllSosRequests();
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
        sosDAO.insertSos(s);
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
        sosDAO.assignTeam(next.getSosId(), team.getTeamId(), ASSIGNED);
        pendingQueue.removeFirst();
        next.setStatus(ASSIGNED);
        next.setTeamId(team.getTeamId());
        return next;
    }

    // Changes the status of an SOS. Only the next step in the chain is allowed.
    public void changeStatus(String sosId, String newStatus) throws InvalidInputException, DatabaseException {
        SOSRequest s = sosDAO.getSosById(sosId);
        if (s == null) {
            throw new InvalidInputException("No SOS with ID " + sosId);
        }
        if (!isValidJump(s.getStatus(), newStatus)) {
            throw new InvalidInputException("Invalid status change: " + s.getStatus() + " to " + newStatus);
        }
        sosDAO.updateStatus(sosId, newStatus);
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
        return sosDAO.getSosByStatus(ASSIGNED);
    }

    // Returns the SOS requests raised by one user.
    public ArrayList<SOSRequest> getSosForUser(int userId) throws DatabaseException {
        return sosDAO.getSosByUser(userId);
    }
}
