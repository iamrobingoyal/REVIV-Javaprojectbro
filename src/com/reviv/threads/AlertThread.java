// src/com/reviv/threads/AlertThread.java
package com.reviv.threads;

public class AlertThread extends Thread {
    // Number of alert lines printed.
    private static final int ALERT_LINES = 3;
    // Pause between two alert lines in milliseconds.
    private static final int DELAY_MS = 1000;
    // Zone and message of this alert (never changed after creation).
    private String zone;
    private String message;

    // Creates an alert thread for one zone.
    public AlertThread(String zone, String message) {
        this.zone = zone;
        this.message = message;
    }

    // Runs in the background: prints the alert line 3 times with a pause.
    @Override
    public void run() {
        for (int i = 1; i <= ALERT_LINES; i++) {
            System.out.println("[ALERT " + i + "/" + ALERT_LINES + "] " + zone + ": " + message);
            if (i < ALERT_LINES) {
                pause();
            }
        }
    }

    // Sleeps for a short time between alert lines.
    private void pause() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            System.out.println("Alert for " + zone + " was interrupted");
        }
    }
}
