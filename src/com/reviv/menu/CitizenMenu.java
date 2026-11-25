// src/com/reviv/menu/CitizenMenu.java
package com.reviv.menu;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.DuplicateSOSException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.SOSRequest;
import com.reviv.service.AlertService;
import com.reviv.service.SOSService;
import com.reviv.user.Session;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;

public class CitizenMenu {
    private InputHelper input;
    private Session session;
    private SOSService sosService;
    private AlertService alertService;

    // Creates the citizen menu with the services it needs.
    public CitizenMenu(InputHelper input, Session session, SOSService sosService, AlertService alertService) {
        this.input = input;
        this.session = session;
        this.sosService = sosService;
        this.alertService = alertService;
    }

    // Shows the citizen menu until the citizen logs out.
    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                running = handleChoice(input.readInt("Choose option: "));
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (DuplicateSOSException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (DatabaseException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Prints the citizen options.
    private void printMenu() {
        ConsoleUI.printBanner("CITIZEN MENU");
        ConsoleUI.printMenuLine("1. Raise SOS");
        ConsoleUI.printMenuLine("2. View my SOS status");
        ConsoleUI.printMenuLine("3. View active alerts");
        ConsoleUI.printMenuLine("0. Logout");
    }

    // Runs one option. Returns false on logout.
    private boolean handleChoice(int choice) throws InvalidInputException, DuplicateSOSException, DatabaseException {
        switch (choice) {
            case 1: raiseSos(); return true;
            case 2: showMySos(); return true;
            case 3: ConsoleUI.printAlerts(alertService.getRecentAlerts()); return true;
            case 0: return false;
            default: throw new InvalidInputException("Wrong menu option");
        }
    }

    // Reads the SOS details and saves the request.
    private void raiseSos() throws InvalidInputException, DuplicateSOSException, DatabaseException {
        String sosId = input.readText("SOS ID (example SOS101): ").toUpperCase();
        String zone = input.readText("Zone (example Zone A): ");
        String description = input.readText("Describe the emergency: ");
        int userId = session.getCurrentUser().getId();
        sosService.addSosRequest(new SOSRequest(sosId, userId, description, zone, SOSService.REPORTED, 0));
        ConsoleUI.printSuccess("SOS " + sosId + " raised. Help is on the way.");
    }

    // Shows only the SOS requests of the logged-in citizen.
    private void showMySos() throws DatabaseException {
        int userId = session.getCurrentUser().getId();
        ConsoleUI.printSosList(sosService.getSosForUser(userId));
    }
}
