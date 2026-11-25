// src/com/reviv/util/ConsoleUI.java
package com.reviv.util;

import com.reviv.model.Alert;
import com.reviv.model.Donation;
import com.reviv.model.Hospital;
import com.reviv.model.RescueTeam;
import com.reviv.model.Resource;
import com.reviv.model.SOSRequest;
import com.reviv.model.Shelter;
import com.reviv.model.Volunteer;
import com.reviv.user.User;
import java.util.ArrayList;
import java.util.HashMap;

public class ConsoleUI {
    // Width of the banner and separator lines.
    private static final int LINE_WIDTH = 60;

    // Builds a line made of the same character.
    private static String line(char c) {
        String text = "";
        for (int i = 0; i < LINE_WIDTH; i++) {
            text = text + c;
        }
        return text;
    }

    // Adds spaces after the text so table columns line up.
    private static String pad(String text, int width) {
        String result = text;
        while (result.length() < width) {
            result = result + " ";
        }
        return result;
    }

    // Prints the REVIV banner with a title.
    public static void printBanner(String title) {
        System.out.println(line('='));
        System.out.println("  REVIV - Emergency Response & Disaster Coordination");
        System.out.println(line('='));
        System.out.println("  " + title);
        System.out.println(line('='));
    }

    // Prints a separator line.
    public static void printSeparator() {
        System.out.println(line('-'));
    }

    // Prints one menu option line.
    public static void printMenuLine(String text) {
        System.out.println("  " + text);
    }

    // Prints one table row from the given cells.
    public static void printRow(String[] cells) {
        String row = "";
        for (int i = 0; i < cells.length; i++) {
            row = row + pad(cells[i], 16);
        }
        System.out.println(row);
    }

    // Prints a success message.
    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    // Prints an error message.
    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    // Prints an information message.
    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    // Prints a table of SOS requests.
    public static void printSosList(ArrayList<SOSRequest> list) {
        printRow(new String[] {"SOS ID", "ZONE", "STATUS", "TEAM", "DESCRIPTION"});
        printSeparator();
        for (SOSRequest s : list) {
            printRow(new String[] {s.getSosId(), s.getZone(), s.getStatus(), "" + s.getTeamId(), s.getDescription()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of rescue teams.
    public static void printTeams(ArrayList<RescueTeam> list) {
        printRow(new String[] {"ID", "TEAM", "CAPTAIN", "FREE?"});
        printSeparator();
        for (RescueTeam t : list) {
            printRow(new String[] {"" + t.getTeamId(), t.getTeamName(), t.getCaptain(), "" + t.isAvailable()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of resources with stock and usage.
    public static void printResources(ArrayList<Resource> list) {
        printRow(new String[] {"ID", "RESOURCE", "IN STOCK", "USED"});
        printSeparator();
        for (Resource r : list) {
            printRow(new String[] {"" + r.getResourceId(), r.getResourceName(), "" + r.getQuantity(), "" + r.getUsedQuantity()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of shelters.
    public static void printShelters(ArrayList<Shelter> list) {
        printRow(new String[] {"ID", "SHELTER", "ZONE", "CAPACITY", "FREE"});
        printSeparator();
        for (Shelter s : list) {
            printRow(new String[] {"" + s.getShelterId(), s.getShelterName(), s.getZone(), "" + s.getCapacity(), "" + s.getFreeCapacity()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of hospitals.
    public static void printHospitals(ArrayList<Hospital> list) {
        printRow(new String[] {"ID", "HOSPITAL", "ZONE", "FREE BEDS"});
        printSeparator();
        for (Hospital h : list) {
            printRow(new String[] {"" + h.getHospitalId(), h.getHospitalName(), h.getZone(), "" + h.getFreeBeds()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of volunteers.
    public static void printVolunteers(ArrayList<Volunteer> list) {
        printRow(new String[] {"ID", "NAME", "SKILL", "AVAILABLE"});
        printSeparator();
        for (Volunteer v : list) {
            printRow(new String[] {"" + v.getVolunteerId(), v.getName(), v.getSkill(), "" + v.isAvailable()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of alerts.
    public static void printAlerts(ArrayList<Alert> list) {
        printRow(new String[] {"ID", "ZONE", "SOS", "MESSAGE"});
        printSeparator();
        for (Alert a : list) {
            printRow(new String[] {"" + a.getAlertId(), a.getZone(), a.getSosId(), a.getMessage()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of donations.
    public static void printDonations(ArrayList<Donation> list) {
        printRow(new String[] {"ID", "DONOR", "AMOUNT"});
        printSeparator();
        for (Donation d : list) {
            printRow(new String[] {"" + d.getDonationId(), d.getDonor(), "" + d.getAmount()});
        }
        printEmptyNote(list.size());
    }

    // Prints a table of users (passwords are never shown).
    public static void printUsers(ArrayList<User> list) {
        printRow(new String[] {"ID", "USERNAME", "ROLE"});
        printSeparator();
        for (User u : list) {
            printRow(new String[] {"" + u.getId(), u.getUsername(), u.getRole()});
        }
        printEmptyNote(list.size());
    }

    // Prints a report table from a HashMap of name and count.
    public static void printCountMap(String nameTitle, HashMap<String, Integer> map) {
        printRow(new String[] {nameTitle, "COUNT"});
        printSeparator();
        for (String key : map.keySet()) {
            printRow(new String[] {key, "" + map.get(key)});
        }
        printEmptyNote(map.size());
    }

    // Prints a note when a table has no rows.
    private static void printEmptyNote(int rows) {
        if (rows == 0) {
            System.out.println("(nothing to show)");
        }
    }
}
