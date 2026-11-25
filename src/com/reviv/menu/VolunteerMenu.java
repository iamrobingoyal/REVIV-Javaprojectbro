// src/com/reviv/menu/VolunteerMenu.java
package com.reviv.menu;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.service.AlertService;
import com.reviv.service.ShelterService;
import com.reviv.service.VolunteerService;
import com.reviv.user.Session;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;

public class VolunteerMenu {
    private InputHelper input;
    private Session session;
    private VolunteerService volunteerService;
    private AlertService alertService;
    private ShelterService shelterService;

    // Creates the volunteer menu with the services it needs.
    public VolunteerMenu(InputHelper input, Session session, VolunteerService volunteerService,
            AlertService alertService, ShelterService shelterService) {
        this.input = input;
        this.session = session;
        this.volunteerService = volunteerService;
        this.alertService = alertService;
        this.shelterService = shelterService;
    }

    // Shows the volunteer menu until the volunteer logs out.
    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                running = handleChoice(input.readInt("Choose option: "));
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (DatabaseException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
    }

    // Prints the volunteer options.
    private void printMenu() {
        ConsoleUI.printBanner("VOLUNTEER MENU");
        ConsoleUI.printMenuLine("1. Update my availability");
        ConsoleUI.printMenuLine("2. View active alerts");
        ConsoleUI.printMenuLine("3. View shelters");
        ConsoleUI.printMenuLine("0. Logout");
    }

    // Runs one option. Returns false on logout.
    private boolean handleChoice(int choice) throws InvalidInputException, DatabaseException {
        switch (choice) {
            case 1: updateAvailability(); return true;
            case 2: ConsoleUI.printAlerts(alertService.getRecentAlerts()); return true;
            case 3: ConsoleUI.printShelters(shelterService.getAllShelters()); return true;
            case 0: return false;
            default: throw new InvalidInputException("Wrong menu option");
        }
    }

    // Saves the availability chosen by the volunteer.
    private void updateAvailability() throws InvalidInputException, DatabaseException {
        int status = input.readInt("1 = available, 2 = not available: ");
        if (status != 1 && status != 2) {
            throw new InvalidInputException("Choose 1 or 2");
        }
        volunteerService.updateAvailability(session.getCurrentUser().getId(), status == 1);
        ConsoleUI.printSuccess("Availability updated");
    }
}
