// src/com/reviv/service/AlertService.java
package com.reviv.service;

import com.reviv.database.AlertDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Alert;
import java.util.ArrayList;
import java.util.LinkedList;

public class AlertService {
    // Number of alerts kept in the recent list.
    private static final int MAX_ALERTS = 10;
    // Sos id used for alerts that are not linked to one SOS.
    public static final String BROADCAST_ID = "BROADCAST";
    // Last 10 alerts: addLast adds, removeFirst drops the oldest.
    private LinkedList<Alert> recentAlerts = new LinkedList<Alert>();
    // DAO used to save and load alerts.
    private AlertDAO alertDAO = new AlertDAO();

    // Saves a new alert in the database and in the recent list.
    public void createAlert(String sosId, String zone, String message)
            throws InvalidInputException, DatabaseException {
        if (zone == null || zone.trim().length() == 0 || message == null || message.trim().length() == 0) {
            throw new InvalidInputException("Zone and message cannot be empty");
        }
        alertDAO.insertAlert(sosId, zone, message);
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
        ArrayList<Alert> all = alertDAO.getAllAlerts();
        for (Alert a : all) {
            addToRecent(a);
        }
    }

    // Returns the last 10 alerts (fresh from the database).
    public ArrayList<Alert> getRecentAlerts() throws DatabaseException {
        loadAlerts();
        return new ArrayList<Alert>(recentAlerts);
    }
}
