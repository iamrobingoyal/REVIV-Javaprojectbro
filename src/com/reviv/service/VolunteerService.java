// src/com/reviv/service/VolunteerService.java
package com.reviv.service;

import com.reviv.database.VolunteerDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Volunteer;
import java.util.ArrayList;
import java.util.HashSet;

public class VolunteerService {
    // Skill given to a volunteer who registers on their own.
    private static final String DEFAULT_SKILL = "General";
    // Volunteers loaded from the database.
    private ArrayList<Volunteer> volunteers = new ArrayList<Volunteer>();
    // DAO used to read and change volunteers.
    private VolunteerDAO volunteerDAO = new VolunteerDAO();

    // Reloads the volunteers from the database.
    public void loadVolunteers() throws DatabaseException {
        volunteers = volunteerDAO.getAllVolunteers();
    }

    // Returns all volunteers (fresh from the database).
    public ArrayList<Volunteer> getAllVolunteers() throws DatabaseException {
        loadVolunteers();
        return new ArrayList<Volunteer>(volunteers);
    }

    // Returns the unique skills of all volunteers (HashSet removes duplicates).
    public ArrayList<String> getUniqueSkills() throws DatabaseException {
        loadVolunteers();
        HashSet<String> skills = new HashSet<String>();
        for (Volunteer v : volunteers) {
            skills.add(v.getSkill());
        }
        return new ArrayList<String>(skills);
    }

    // Creates the volunteer profile of a newly registered volunteer user.
    public void createVolunteerProfile(String name, int userId) throws DatabaseException {
        volunteerDAO.insertVolunteer(name, DEFAULT_SKILL, userId);
    }

    // Changes the availability of the logged-in volunteer.
    public void updateAvailability(int userId, boolean available) throws InvalidInputException, DatabaseException {
        if (volunteerDAO.updateAvailability(userId, available) == 0) {
            throw new InvalidInputException("No volunteer profile found for this user");
        }
    }
}
