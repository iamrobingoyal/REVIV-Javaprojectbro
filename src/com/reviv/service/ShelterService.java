// src/com/reviv/service/ShelterService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.model.Shelter;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ShelterService {
    // Shelters loaded from the database.
    private ArrayList<Shelter> shelters = new ArrayList<Shelter>();

    // Reloads the shelters from the database.
    public void loadShelters() throws DatabaseException {
        shelters = dbGetAllShelters();
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
        if (reduceFreeCapacity(shelterId, count) == 0) {
            throw new ResourceUnavailableException(shelter.getShelterName() + " has only "
                    + shelter.getFreeCapacity() + " free places");
        }
        return shelter;
    }

    // Direct JDBC operations for shelters.

    // Returns all shelters from the database.
    public ArrayList<Shelter> dbGetAllShelters() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT shelter_id, shelter_name, zone, capacity, free_capacity FROM shelters ORDER BY shelter_id");
            rs = ps.executeQuery();
            return toShelterList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load shelters: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Reduces free capacity. Returns 0 rows when there is not enough room.
    public int reduceFreeCapacity(int shelterId, int count) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE shelters SET free_capacity = free_capacity - ? WHERE shelter_id = ? AND free_capacity >= ?");
            ps.setInt(1, count);
            ps.setInt(2, shelterId);
            ps.setInt(3, count);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update shelter: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the shelter result set into a list of shelters.
    private ArrayList<Shelter> toShelterList(ResultSet rs) throws SQLException {
        ArrayList<Shelter> list = new ArrayList<Shelter>();
        while (rs.next()) {
            list.add(new Shelter(rs.getInt("shelter_id"), rs.getString("shelter_name"), rs.getString("zone"),
                    rs.getInt("capacity"), rs.getInt("free_capacity")));
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
