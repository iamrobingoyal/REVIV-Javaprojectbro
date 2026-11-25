// src/com/reviv/database/DonationDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.model.Donation;

public class DonationDAO {
    // Returns all donations from the database.
    public ArrayList<Donation> getAllDonations() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT donation_id, donor, amount FROM donations ORDER BY donation_id");
            rs = ps.executeQuery();
            return toDonationList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load donations: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves a new donation.
    public void insertDonation(String donor, double amount) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO donations (donor, amount) VALUES (?, ?)");
            ps.setString(1, donor);
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save donation: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the donation result set into a list of donations.
    private ArrayList<Donation> toDonationList(ResultSet rs) throws SQLException {
        ArrayList<Donation> list = new ArrayList<Donation>();
        while (rs.next()) {
            list.add(new Donation(rs.getInt("donation_id"), rs.getString("donor"), rs.getDouble("amount")));
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
