// src/com/reviv/threads/ResourceMonitorTask.java
package com.reviv.threads;

import com.reviv.service.ResourceService;
import com.reviv.exception.DatabaseException;
import com.reviv.model.Resource;
import java.util.ArrayList;

public class ResourceMonitorTask implements Runnable {
    // Stock below this number triggers a warning.
    private static final int LOW_STOCK_LIMIT = 20;
    // Time between two checks in milliseconds.
    private static final int CHECK_INTERVAL_MS = 8000;
    // This task reads inventory through the resource service.
    private ResourceService resourceService = new ResourceService();

    // Checks the stock again and again until the thread is interrupted.
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkStock();
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println("[Resource monitor stopped]");
    }

    // Reads the stock from the database and prints low-stock warnings.
    private void checkStock() {
        try {
            ArrayList<Resource> list = resourceService.getAllResources();
            for (Resource r : list) {
                if (r.getQuantity() < LOW_STOCK_LIMIT) {
                    System.out.println("[LOW STOCK WARNING] " + r.getResourceName() + " has only " + r.getQuantity() + " left");
                }
            }
        } catch (DatabaseException e) {
            System.out.println("Stock check failed: " + e.getMessage());
        }
    }
}
