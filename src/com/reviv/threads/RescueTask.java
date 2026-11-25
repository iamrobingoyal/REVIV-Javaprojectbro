// src/com/reviv/threads/RescueTask.java
package com.reviv.threads;

import com.reviv.database.SOSDAO;
import com.reviv.database.TeamDAO;
import com.reviv.exception.DatabaseException;

public class RescueTask implements Runnable {
    // Number of rescue steps and the time of each step in milliseconds.
    private static final int RESCUE_STEPS = 3;
    private static final int STEP_MS = 1500;
    // Data of this rescue (text and numbers only, never changed).
    private String sosId;
    private int teamId;
    private String teamName;

    // Creates a rescue task for one SOS and one team.
    public RescueTask(String sosId, int teamId, String teamName) {
        this.sosId = sosId;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    // Simulates the rescue, then saves RESOLVED and frees the team in the database.
    @Override
    public void run() {
        System.out.println("[RESCUE] " + teamName + " started " + sosId);
        if (!simulateSteps()) {
            return;
        }
        try {
            new SOSDAO().updateStatus(sosId, "RESOLVED");
            new TeamDAO().setAvailable(teamId, true);
            System.out.println("[RESCUE] " + teamName + " finished " + sosId + " and is free again");
        } catch (DatabaseException e) {
            System.out.println("[RESCUE] Could not save result of " + sosId + ": " + e.getMessage());
        }
    }

    // Sleeps through the rescue steps. Returns false if interrupted.
    private boolean simulateSteps() {
        for (int step = 1; step <= RESCUE_STEPS; step++) {
            try {
                Thread.sleep(STEP_MS);
            } catch (InterruptedException e) {
                System.out.println("[RESCUE] " + teamName + " was interrupted");
                return false;
            }
            System.out.println("[RESCUE] " + teamName + " step " + step + "/" + RESCUE_STEPS + " on " + sosId);
        }
        return true;
    }
}
