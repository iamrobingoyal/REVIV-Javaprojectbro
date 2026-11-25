// src/com/reviv/database/VolunteerDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.Volunteer;

public class VolunteerDAO {
    // Returns all volunteers from the database.
    public ArrayList<Volunteer> getAllVolunteers() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT volunteer_id, name, skill, user_id, available FROM volunteers ORDER BY volunteer_id");
            rs = ps.executeQuery();
            return toVolunteerList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load volunteers: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves a new volunteer profile linked to a user.
    public void insertVolunteer(String name, String skill, int userId) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO volunteers (name, skill, user_id, available) VALUES (?, ?, ?, TRUE)");
            ps.setString(1, name);
            ps.setString(2, skill);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add volunteer: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Changes availability of the volunteer linked to a user. Returns changed rows.
    public int updateAvailability(int userId, boolean available) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE volunteers SET available = ? WHERE user_id = ?");
            ps.setBoolean(1, available);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update availability: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the volunteer result set into a list of volunteers.
    private ArrayList<Volunteer> toVolunteerList(ResultSet rs) throws SQLException {
        ArrayList<Volunteer> list = new ArrayList<Volunteer>();
        while (rs.next()) {
            list.add(new Volunteer(rs.getInt("volunteer_id"), rs.getString("name"), rs.getString("skill"),
                    rs.getInt("user_id"), rs.getBoolean("available")));
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
