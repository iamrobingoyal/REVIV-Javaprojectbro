// src/com/reviv/threads/EscalationThread.java
package com.reviv.threads;

import com.reviv.database.SOSDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.model.SOSRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class EscalationThread extends Thread {
    // Time between two scans in milliseconds.
    private static final int SCAN_INTERVAL_MS = 5000;
    // A request is escalated after this many scans while still pending.
    private static final int WARN_AFTER_SCANS = 2;
    // This thread has its own DAO, so it never shares data with the menu.
    private SOSDAO sosDAO = new SOSDAO();
    // Age of each pending SOS counted in scans (simple int counter).
    private HashMap<String, Integer> ages = new HashMap<String, Integer>();

    // Runs in the background until it is interrupted (stopped by the admin).
    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(SCAN_INTERVAL_MS);
                scanPending();
            } catch (InterruptedException e) {
                break;
            } catch (DatabaseException e) {
                System.out.println("Escalation scan failed: " + e.getMessage());
            }
        }
        System.out.println("[Escalation monitor stopped]");
    }

    // Reads pending SOS from the database and warns about the old ones.
    private void scanPending() throws DatabaseException {
        ArrayList<SOSRequest> pending = sosDAO.getSosByStatus("REPORTED");
        HashMap<String, Integer> newAges = new HashMap<String, Integer>();
        for (SOSRequest s : pending) {
            int age = getAge(s.getSosId()) + 1;
            newAges.put(s.getSosId(), age);
            if (age >= WARN_AFTER_SCANS) {
                System.out.println("[ESCALATION WARNING] " + s.getSosId() + " in " + s.getZone()
                        + " is still pending (age " + age + " scans)");
            }
        }
        ages = newAges;
    }

    // Returns the saved age of an SOS (0 if new).
    private int getAge(String sosId) {
        Integer age = ages.get(sosId);
        if (age == null) {
            return 0;
        }
        return age.intValue();
    }
}
