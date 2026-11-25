// src/com/reviv/database/HospitalDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.Hospital;

public class HospitalDAO {
    // Returns all hospitals from the database.
    public ArrayList<Hospital> getAllHospitals() throws DatabaseException {
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
