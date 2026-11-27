// src/com/reviv/service/HospitalService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Hospital;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class HospitalService {
    // Hospitals loaded from the database.
    private ArrayList<Hospital> hospitals = new ArrayList<Hospital>();

    // Reloads the hospitals from the database.
    public void loadHospitals() throws DatabaseException {
        hospitals = dbGetAllHospitals();
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
        return reduceFreeBeds(hospitalId, 1) > 0;
    }

    // Returns the name of a hospital (empty text if unknown).
    public String getHospitalName(int hospitalId) {
        Hospital h = findHospital(hospitalId);
        if (h == null) {
            return "";
        }
        return h.getHospitalName();
    }

    // Direct JDBC operations for hospitals.

    // Returns all hospitals from the database.
    public ArrayList<Hospital> dbGetAllHospitals() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT hospital_id, hospital_name, zone, free_beds FROM hospitals ORDER BY hospital_id");
            rs = ps.executeQuery();
            return toHospitalList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load hospitals: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Reduces free beds. Returns 0 rows when there are not enough beds.
    public int reduceFreeBeds(int hospitalId, int count) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE hospitals SET free_beds = free_beds - ? WHERE hospital_id = ? AND free_beds >= ?");
            ps.setInt(1, count);
            ps.setInt(2, hospitalId);
            ps.setInt(3, count);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update hospital: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the hospital result set into a list of hospitals.
    private ArrayList<Hospital> toHospitalList(ResultSet rs) throws SQLException {
        ArrayList<Hospital> list = new ArrayList<Hospital>();
        while (rs.next()) {
            list.add(new Hospital(rs.getInt("hospital_id"), rs.getString("hospital_name"),
                    rs.getString("zone"), rs.getInt("free_beds")));
        }
        return list;
    }

    // Closes result set, statement and connection safely.
    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close ResultSet");
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close PreparedStatement");
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close Connection");
        }
    }

}
