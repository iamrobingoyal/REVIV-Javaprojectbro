// src/com/reviv/Main.java
package com.reviv;

import com.reviv.database.DatabaseInitializer;
import com.reviv.exception.DatabaseException;
import com.reviv.menu.AdminMenu;
import com.reviv.menu.CitizenMenu;
import com.reviv.menu.DispatcherMenu;
import com.reviv.menu.MainMenu;
import com.reviv.menu.VolunteerMenu;
import com.reviv.service.AlertService;
import com.reviv.service.DonationService;
import com.reviv.service.HospitalService;
import com.reviv.service.ReportService;
import com.reviv.service.ResourceService;
import com.reviv.service.SOSService;
import com.reviv.service.ShelterService;
import com.reviv.service.TeamService;
import com.reviv.service.VolunteerService;
import com.reviv.service.ZoneService;
import com.reviv.user.Session;
import com.reviv.user.UserService;
import com.reviv.util.ConsoleUI;
import com.reviv.util.InputHelper;

public class Main {
    // Shared objects of the whole program.
    private InputHelper input = new InputHelper();
    private Session session = new Session();
    private UserService userService = new UserService();
    private ZoneService zoneService = new ZoneService();
    private SOSService sosService = new SOSService(zoneService);
    private TeamService teamService = new TeamService();
    private ResourceService resourceService = new ResourceService();
    private AlertService alertService = new AlertService();
    private ShelterService shelterService = new ShelterService();
    private HospitalService hospitalService = new HospitalService();
    private VolunteerService volunteerService = new VolunteerService();
    private DonationService donationService = new DonationService();
    private ReportService reportService = new ReportService();

    // Program starts here.
    public static void main(String[] args) {
        new Main().start();
    }

    // Prepares the database, loads the data and opens the main menu.
    private void start() {
        try {
            DatabaseInitializer.initialize();
            loadStartupData();
        } catch (DatabaseException e) {
            ConsoleUI.printError(e.getMessage());
            ConsoleUI.printInfo("Check MySQL is running and the settings in DBConnection.java");
            return;
        }
        MainMenu mainMenu = buildMenus();
        mainMenu.run();
    }

    // Loads the data that the services keep in memory.
    private void loadStartupData() throws DatabaseException {
        sosService.loadFromDatabase();
        resourceService.loadResources();
        teamService.loadTeams();
        alertService.loadAlerts();
    }

    // Creates every menu and connects them to the services.
    private MainMenu buildMenus() {
        DispatcherMenu dispatcher = new DispatcherMenu(input, sosService, teamService, resourceService,
                alertService, hospitalService, shelterService, zoneService);
        AdminMenu admin = new AdminMenu(input, dispatcher, resourceService, teamService,
                donationService, reportService, userService);
        CitizenMenu citizen = new CitizenMenu(input, session, sosService, alertService);
        VolunteerMenu volunteer = new VolunteerMenu(input, session, volunteerService, alertService, shelterService);
        return new MainMenu(input, session, userService, volunteerService, admin, dispatcher, citizen, volunteer);
    }
}