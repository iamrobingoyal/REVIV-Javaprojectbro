// src/com/reviv/menu/AdminMenu.java
package com.reviv.menu;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.service.DonationService;
import com.reviv.service.ReportService;
import com.reviv.service.ResourceService;
import com.reviv.service.TeamService;
import com.reviv.threads.EscalationThread;
import com.reviv.threads.ResourceMonitorTask;
import com.reviv.user.UserService;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;

public class AdminMenu {
    private InputHelper input;
    private DispatcherMenu dispatcherMenu;
    private ResourceService resourceService;
    private TeamService teamService;
    private DonationService donationService;
    private ReportService reportService;
    private UserService userService;
    // Background threads that the admin can start and stop.
    private Thread monitorThread;
    private Thread escalationThread;

    // Creates the admin menu with the services it needs.
    public AdminMenu(InputHelper input, DispatcherMenu dispatcherMenu, ResourceService resourceService,
            TeamService teamService, DonationService donationService, ReportService reportService,
            UserService userService) {
        this.input = input;
        this.dispatcherMenu = dispatcherMenu;
        this.resourceService = resourceService;
        this.teamService = teamService;
        this.donationService = donationService;
        this.reportService = reportService;
        this.userService = userService;
    }

    // Shows the admin menu until the admin logs out.
    public void show() {
        boolean running = true;
        try {
            while (running) {
                printMenu();
                running = readAndRun();
            }
        } finally {
            stopMonitors();
        }
    }

    // Reads one choice and runs it. Returns false on logout.
    private boolean readAndRun() {
        try {
            int choice = input.readInt("Choose option: ");
            if (choice == 0) {
                return false;
            }
            if (choice >= 1 && choice <= 9) {
                dispatcherMenu.runOption(choice);
            } else {
                runAdminOption(choice);
            }
        } catch (InvalidInputException e) {
            ConsoleUI.printError(e.getMessage());
        } catch (TeamBusyException e) {
            ConsoleUI.printError(e.getMessage());
        } catch (ResourceUnavailableException e) {
            ConsoleUI.printError(e.getMessage());
        } catch (DatabaseException e) {
            ConsoleUI.printError(e.getMessage());
        }
        return true;
    }

    // Prints all admin options.
    private void printMenu() {
        ConsoleUI.printBanner("ADMIN MENU");
        dispatcherMenu.printDispatcherOptions();
        ConsoleUI.printMenuLine("10. Add resource stock");
        ConsoleUI.printMenuLine("11. View resource stock");
        ConsoleUI.printMenuLine("12. Add rescue team");
        ConsoleUI.printMenuLine("13. Change team availability");
        ConsoleUI.printMenuLine("14. Add donation");
        ConsoleUI.printMenuLine("15. View donations");
        ConsoleUI.printMenuLine("16. Report: incidents per zone");
        ConsoleUI.printMenuLine("17. Report: team workload");
        ConsoleUI.printMenuLine("18. Report: resource usage");
        ConsoleUI.printMenuLine("19. Start monitoring threads");
        ConsoleUI.printMenuLine("20. Stop monitoring threads");
        ConsoleUI.printMenuLine("21. View all users");
        ConsoleUI.printMenuLine("0. Logout");
    }

    // Runs one admin-only option (10 to 21).
    private void runAdminOption(int choice) throws InvalidInputException, DatabaseException {
        switch (choice) {
            case 10: addStock(); break;
            case 11: ConsoleUI.printResources(resourceService.getAllResources()); break;
            case 12: addTeam(); break;
            case 13: changeTeamAvailability(); break;
            case 14: addDonation(); break;
            case 15: showDonations(); break;
            case 16: ConsoleUI.printCountMap("ZONE", reportService.getIncidentsPerZone()); break;
            case 17: ConsoleUI.printCountMap("TEAM", reportService.getTeamWorkload()); break;
            case 18: ConsoleUI.printResources(reportService.getResourceUsage()); break;
            case 19: startMonitors(); break;
            case 20: stopMonitors(); break;
            case 21: ConsoleUI.printUsers(userService.getAllUsers()); break;
            default: throw new InvalidInputException("Wrong menu option");
        }
    }

    // Adds stock to a resource (new names create a new resource).
    private void addStock() throws InvalidInputException, DatabaseException {
        String name = input.readText("Resource name: ");
        int quantity = input.readPositiveInt("Quantity to add: ");
        resourceService.addStock(name, quantity);
        ConsoleUI.printSuccess("Stock updated for " + name);
    }

    // Adds a new rescue team.
    private void addTeam() throws InvalidInputException, DatabaseException {
        String teamName = input.readText("Team name: ");
        String captain = input.readText("Captain name: ");
        teamService.addTeam(teamName, captain);
        ConsoleUI.printSuccess("Team added: " + teamName);
    }

    // Changes a team to available or busy.
    private void changeTeamAvailability() throws InvalidInputException, DatabaseException {
        ConsoleUI.printTeams(teamService.getAllTeams());
        int teamId = input.readPositiveInt("Enter team ID: ");
        int status = input.readInt("1 = available, 2 = busy: ");
        if (status != 1 && status != 2) {
            throw new InvalidInputException("Choose 1 or 2");
        }
        teamService.setTeamAvailability(teamId, status == 1);
        ConsoleUI.printSuccess("Team updated");
    }

    // Saves a new donation.
    private void addDonation() throws InvalidInputException, DatabaseException {
        String donor = input.readText("Donor name: ");
        double amount = input.readPositiveDouble("Amount: ");
        donationService.addDonation(donor, amount);
        ConsoleUI.printSuccess("Thank you " + donor + ", donation saved");
    }

    // Shows all donations and their total.
    private void showDonations() throws DatabaseException {
        ConsoleUI.printDonations(donationService.getAllDonations());
        ConsoleUI.printInfo("Total donated: " + donationService.getTotalAmount());
    }

    // Starts the resource monitor and the escalation monitor threads.
    private void startMonitors() {
        if (isRunning(monitorThread) || isRunning(escalationThread)) {
            ConsoleUI.printInfo("Monitoring threads are already running");
            return;
        }
        monitorThread = new Thread(new ResourceMonitorTask());
        escalationThread = new EscalationThread();
        monitorThread.setDaemon(true);
        escalationThread.setDaemon(true);
        monitorThread.start();
        escalationThread.start();
        ConsoleUI.printSuccess("Monitoring threads started");
    }

    // Stops the monitoring threads using interrupt() and waits for them.
    private void stopMonitors() {
        stopThread(monitorThread);
        stopThread(escalationThread);
    }

    // Returns true when the thread exists and is still running.
    private boolean isRunning(Thread t) {
        return t != null && t.isAlive();
    }

    // Interrupts one thread and waits until it ends.
    private void stopThread(Thread t) {
        if (!isRunning(t)) {
            return;
        }
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            ConsoleUI.printError("Waiting was interrupted");
        }
    }
}
