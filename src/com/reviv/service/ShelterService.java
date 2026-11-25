// src/com/reviv/service/ShelterService.java
package com.reviv.service;

import com.reviv.database.ShelterDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.model.Shelter;
import java.util.ArrayList;

public class ShelterService {
    // Shelters loaded from the database.
    private ArrayList<Shelter> shelters = new ArrayList<Shelter>();
    // DAO used to read and change shelters.
    private ShelterDAO shelterDAO = new ShelterDAO();

    // Reloads the shelters from the database.
    public void loadShelters() throws DatabaseException {
        shelters = shelterDAO.getAllShelters();
    }

    // Returns all shelters (fresh from the database).
    public ArrayList<Shelter> getAllShelters() throws DatabaseException {
        loadShelters();
        return new ArrayList<Shelter>(shelters);
    }

    // Finds a shelter by id in the loaded list, or returns null.
    private Shelter findShelter(int shelterId) {
        for (Shelter s : shelters) {
            if (s.getShelterId() == shelterId) {
                return s;
            }
        }
        return null;
    }

    // Sends evacuees to a shelter and reduces its free capacity. Throws when the shelter is full.
    public Shelter assignEvacuees(int shelterId, int count)
            throws InvalidInputException, ResourceUnavailableException, DatabaseException {
        if (count <= 0) {
            throw new InvalidInputException("Number of evacuees must be greater than zero");
        }
        loadShelters();
        Shelter shelter = findShelter(shelterId);
        if (shelter == null) {
            throw new InvalidInputException("No shelter with ID " + shelterId);
        }
        if (shelterDAO.reduceFreeCapacity(shelterId, count) == 0) {
            throw new ResourceUnavailableException(shelter.getShelterName() + " has only "
                    + shelter.getFreeCapacity() + " free places");
        }
        return shelter;
    }
}
