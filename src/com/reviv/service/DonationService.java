// src/com/reviv/service/DonationService.java
package com.reviv.service;

import com.reviv.database.DonationDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.model.Donation;
import java.util.ArrayList;

public class DonationService {
    // DAO used to save and load donations.
    private DonationDAO donationDAO = new DonationDAO();

    // Saves a new donation after checking the data.
    public void addDonation(String donor, double amount) throws InvalidInputException, DatabaseException {
        if (donor == null || donor.trim().length() == 0) {
            throw new InvalidInputException("Donor name cannot be empty");
        }
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero");
        }
        donationDAO.insertDonation(donor, amount);
    }

    // Returns all donations.
    public ArrayList<Donation> getAllDonations() throws DatabaseException {
        return donationDAO.getAllDonations();
    }

    // Returns the total of all donations.
    public double getTotalAmount() throws DatabaseException {
        double total = 0;
        for (Donation d : donationDAO.getAllDonations()) {
            total = total + d.getAmount();
        }
        return total;
    }
}
