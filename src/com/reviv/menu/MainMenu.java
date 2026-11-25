// src/com/reviv/menu/MainMenu.java
package com.reviv.menu;

import com.reviv.exception.AuthenticationException;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.service.VolunteerService;
import com.reviv.user.Session;
import com.reviv.user.User;
import com.reviv.user.UserService;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;

public class MainMenu {
    private InputHelper input;
    private Session session;
    private UserService userService;
    private VolunteerService volunteerService;
    private AdminMenu adminMenu;
    private DispatcherMenu dispatcherMenu;
    private CitizenMenu citizenMenu;
    private VolunteerMenu volunteerMenu;

    // Creates the main menu with the role menus.
    public MainMenu(InputHelper input, Session session, UserService userService, VolunteerService volunteerService,
            AdminMenu adminMenu, DispatcherMenu dispatcherMenu, CitizenMenu citizenMenu, VolunteerMenu volunteerMenu) {
        this.input = input;
        this.session = session;
        this.userService = userService;
        this.volunteerService = volunteerService;
        this.adminMenu = adminMenu;
        this.dispatcherMenu = dispatcherMenu;
        this.citizenMenu = citizenMenu;
        this.volunteerMenu = volunteerMenu;
    }

    // Runs the main menu until the user chooses Exit.
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                running = handleChoice(input.readInt("Choose option: "));
            } catch (InvalidInputException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (AuthenticationException e) {
                ConsoleUI.printError(e.getMessage());
            } catch (DatabaseException e) {
                ConsoleUI.printError(e.getMessage());
            }
        }
        ConsoleUI.printInfo("Thank you for using REVIV. Stay safe!");
    }

    // Prints the main menu options.
    private void printMenu() {
        ConsoleUI.printBanner("MAIN MENU");
        ConsoleUI.printMenuLine("1. Login");
        ConsoleUI.printMenuLine("2. Register (citizen / volunteer)");
        ConsoleUI.printMenuLine("0. Exit");
    }

    // Runs one option. Returns false when the user exits.
    private boolean handleChoice(int choice) throws InvalidInputException, AuthenticationException, DatabaseException {
        switch (choice) {
            case 1: login(); return true;
            case 2: register(); return true;
            case 0: return false;
            default: throw new InvalidInputException("Wrong menu option");
        }
    }

    // Logs in, opens the menu of the user role and always logs out in finally.
    private void login() throws InvalidInputException, AuthenticationException, DatabaseException {
        String username = input.readText("Username: ");
        String password = input.readText("Password: ");
        User user = userService.loginUser(username, password);
        session.setCurrentUser(user);
        try {
            ConsoleUI.printSuccess("Welcome " + user.getUsername() + " (" + user.getRole() + ")");
            openRoleMenu(user.getRole());
        } finally {
            session.logout();
            ConsoleUI.printInfo("You are logged out");
        }
    }

    // Opens the menu that matches the role of the user.
    private void openRoleMenu(String role) {
        switch (role) {
            case "ADMIN": adminMenu.show(); break;
            case "DISPATCHER": dispatcherMenu.show(); break;
            case "CITIZEN": citizenMenu.show(); break;
            case "VOLUNTEER": volunteerMenu.show(); break;
            default: ConsoleUI.printError("Unknown role: " + role);
        }
    }

    // Registers a new citizen or volunteer.
    private void register() throws InvalidInputException, DatabaseException {
        String username = input.readText("Choose a username: ");
        String password = input.readText("Choose a password: ");
        int roleChoice = input.readInt("Register as 1 = Citizen, 2 = Volunteer: ");
        if (roleChoice != 1 && roleChoice != 2) {
            throw new InvalidInputException("Choose 1 or 2");
        }
        String role = "CITIZEN";
        if (roleChoice == 2) {
            role = "VOLUNTEER";
        }
        User user = userService.registerUser(username, password, role);
        if (roleChoice == 2) {
            volunteerService.createVolunteerProfile(username, user.getId());
        }
        ConsoleUI.printSuccess("Registered " + username + " as " + role + ". You can login now.");
    }
}
