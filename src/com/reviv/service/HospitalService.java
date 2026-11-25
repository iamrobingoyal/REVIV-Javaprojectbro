// src/com/reviv/service/HospitalService.java
package com.reviv.service;

import com.reviv.database.HospitalDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Hospital;
import java.util.ArrayList;

public class HospitalService {
    // Hospitals loaded from the database.
    private ArrayList<Hospital> hospitals = new ArrayList<Hospital>();
    // DAO used to read and change hospitals.
    private HospitalDAO hospitalDAO = new HospitalDAO();

    // Reloads the hospitals from the database.
    public void loadHospitals() throws DatabaseException {
        hospitals = hospitalDAO.getAllHospitals();
    }

    // Returns all hospitals (fresh from the database).
    public ArrayList<Hospital> getAllHospitals() throws DatabaseException {
        loadHospitals();
        return new ArrayList<Hospital>(hospitals);
    }

    // Finds a hospital by id in the loaded list, or returns null.
    private Hospital findHospital(int hospitalId) {
        for (Hospital h : hospitals) {
            if (h.getHospitalId() == hospitalId) {
                return h;
            }
        }
        return null;
    }

    // Allots one patient to a hospital. Returns false when no bed is free.
    public boolean allotPatient(int hospitalId) throws InvalidInputException, DatabaseException {
        loadHospitals();
        if (findHospital(hospitalId) == null) {
            throw new InvalidInputException("No hospital with ID " + hospitalId);
        }
        return hospitalDAO.reduceFreeBeds(hospitalId, 1) > 0;
    }

    // Returns the name of a hospital (empty text if unknown).
    public String getHospitalName(int hospitalId) {
        Hospital h = findHospital(hospitalId);
        if (h == null) {
            return "";
        }
        return h.getHospitalName();
    }
}
