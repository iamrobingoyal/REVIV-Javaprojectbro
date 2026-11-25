// src/com/reviv/database/ShelterDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.Shelter;

public class ShelterDAO {
    // Returns all shelters from the database.
    public ArrayList<Shelter> getAllShelters() throws DatabaseException {
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
