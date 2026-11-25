// src/com/reviv/menu/DispatcherMenu.java
package com.reviv.menu;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.exception.TeamBusyException;
import com.reviv.model.SOSRequest;
import com.reviv.model.Shelter;
import com.reviv.service.AlertService;
import com.reviv.service.HospitalService;
import com.reviv.service.ResourceService;
import com.reviv.service.SOSService;
import com.reviv.service.ShelterService;
import com.reviv.service.TeamService;
import com.reviv.service.ZoneService;
import com.reviv.threads.AlertThread;
import com.reviv.threads.RescueTask;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;
import java.util.ArrayList;

public class DispatcherMenu {
    private InputHelper input;
    private SOSService sosService;
    private TeamService teamService;
    private ResourceService resourceService;
    private AlertService alertService;
    private HospitalService hospitalService;
    private ShelterService shelterService;
    private ZoneService zoneService;

    // Creates the dispatcher menu with the services it needs.
    public DispatcherMenu(InputHelper input, SOSService sosService, TeamService teamService,
            ResourceService resourceService, AlertService alertService, HospitalService hospitalService,
            ShelterService shelterService, ZoneService zoneService) {
        this.input = input;
        this.sosService = sosService;
        this.teamService = teamService;
        this.resourceService = resourceService;
        this.alertService = alertService;
        this.hospitalService = hospitalService;
        this.shelterService = shelterService;
        this.zoneService = zoneService;
    }

    // Shows the dispatcher menu until the dispatcher logs out.
    public void show() {
        boolean running = true;
        while (running) {
            ConsoleUI.printBanner("DISPATCHER MENU");
            printDispatcherOptions();
            ConsoleUI.printMenuLine("0. Logout");
            try {
                int choice = input.readInt("Choose option: ");
                if (choice == 0) {
                    running = false;
                } else {
                    runOption(choice);
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
        }
    }

    // Prints the options 1 to 9 (the admin menu reuses them).
    public void printDispatcherOptions() {
        ConsoleUI.printMenuLine("1. View pending SOS queue");
        ConsoleUI.printMenuLine("2. Process next SOS (assign team + deduct resources)");
        ConsoleUI.printMenuLine("3. View rescue teams");
        ConsoleUI.printMenuLine("4. Start rescue operations (Runnable threads)");
        ConsoleUI.printMenuLine("5. Broadcast alert (Thread)");
        ConsoleUI.printMenuLine("6. View hospitals");
        ConsoleUI.printMenuLine("7. View shelters");
        ConsoleUI.printMenuLine("8. Allot patient to hospital");
        ConsoleUI.printMenuLine("9. Assign evacuees to shelter");
    }

    // Runs one dispatcher option (1 to 9). Wrong numbers are rejected.
    public void runOption(int choice) throws InvalidInputException, TeamBusyException,
            ResourceUnavailableException, DatabaseException {
        switch (choice) {
            case 1: ConsoleUI.printSosList(sosService.getPendingQueue()); break;
            case 2: processNextSos(); break;
            case 3: ConsoleUI.printTeams(teamService.getAllTeams()); break;
            case 4: startRescue(); break;
            case 5: broadcastAlert(); break;
            case 6: ConsoleUI.printHospitals(hospitalService.getAllHospitals()); break;
            case 7: ConsoleUI.printShelters(shelterService.getAllShelters()); break;
            case 8: allotPatient(); break;
            case 9: assignEvacuees(); break;
            default: throw new InvalidInputException("Wrong menu option");
        }
    }

    // Shows the next SOS and the teams, then assigns the chosen team.
    private void processNextSos() throws InvalidInputException, TeamBusyException,
            ResourceUnavailableException, DatabaseException {
        SOSRequest next = sosService.peekNextSos();
        if (next == null) {
            ConsoleUI.printInfo("No pending SOS requests");
            return;
        }
        ConsoleUI.printInfo("Next SOS: " + next.getSosId() + " in " + next.getZone() + " - " + next.getDescription());
        ConsoleUI.printTeams(teamService.getAllTeams());
        int teamId = input.readPositiveInt("Enter team ID to assign: ");
        SOSRequest done = sosService.processNextSos(teamId, teamService, resourceService);
        ConsoleUI.printSuccess(done.getSosId() + " is now ASSIGNED to team " + done.getTeamId());
    }

    // Starts one RescueTask thread for each assigned SOS and waits for all.
    private void startRescue() throws InvalidInputException, DatabaseException {
        ArrayList<SOSRequest> assigned = sosService.getAssignedRequests();
        if (assigned.isEmpty()) {
            ConsoleUI.printInfo("No assigned SOS. Process pending SOS first (option 2)");
            return;
        }
        Thread[] workers = createRescueThreads(assigned);
        ConsoleUI.printInfo("Starting " + workers.length + " rescue task(s) together...");
        for (int i = 0; i < workers.length; i++) {
            workers[i].start();
        }
        waitForAll(workers);
        ConsoleUI.printSuccess("All rescue operations finished");
    }

    // Moves each SOS to IN_PROGRESS and builds a thread with a RescueTask for it.
    private Thread[] createRescueThreads(ArrayList<SOSRequest> assigned) throws InvalidInputException, DatabaseException {
        Thread[] workers = new Thread[assigned.size()];
        for (int i = 0; i < workers.length; i++) {
            SOSRequest s = assigned.get(i);
            sosService.changeStatus(s.getSosId(), SOSService.IN_PROGRESS);
            String teamName = teamService.getTeamName(s.getTeamId());
            workers[i] = new Thread(new RescueTask(s.getSosId(), s.getTeamId(), teamName));
        }
        return workers;
    }

    // Uses join() so the menu waits until every thread is finished.
    private void waitForAll(Thread[] workers) {
        for (int i = 0; i < workers.length; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                ConsoleUI.printError("Waiting was interrupted");
            }
        }
    }

    // Saves an alert and shows it three times using an AlertThread.
    private void broadcastAlert() throws InvalidInputException, DatabaseException {
        ConsoleUI.printInfo("Affected zones so far: " + zoneService.getAllZones());
        String zone = input.readText("Zone to alert: ");
        String message = input.readText("Alert message: ");
        alertService.createAlert(AlertService.BROADCAST_ID, zone, message);
        AlertThread alertThread = new AlertThread(zone, message);
        alertThread.start();
        waitForAll(new Thread[] {alertThread});
        ConsoleUI.printSuccess("Alert broadcast to " + zone);
    }

    // Allots one patient to the chosen hospital.
    private void allotPatient() throws InvalidInputException, DatabaseException {
        ConsoleUI.printHospitals(hospitalService.getAllHospitals());
        int hospitalId = input.readPositiveInt("Enter hospital ID: ");
        if (hospitalService.allotPatient(hospitalId)) {
            ConsoleUI.printSuccess("Patient admitted. One bed used.");
        } else {
            ConsoleUI.printInfo("No free beds in this hospital. Choose another hospital.");
        }
    }

    // Sends evacuees to the chosen shelter.
    private void assignEvacuees() throws InvalidInputException, ResourceUnavailableException, DatabaseException {
        ConsoleUI.printShelters(shelterService.getAllShelters());
        int shelterId = input.readPositiveInt("Enter shelter ID: ");
        int count = input.readPositiveInt("Number of evacuees: ");
        Shelter shelter = shelterService.assignEvacuees(shelterId, count);
        ConsoleUI.printSuccess(count + " evacuee(s) sent to " + shelter.getShelterName());
    }
}
