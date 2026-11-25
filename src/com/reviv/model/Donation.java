// src/com/reviv/model/Donation.java
package com.reviv.model;

public class Donation {
    private int donationId;
    private String donor;
    private double amount;

    // Empty constructor for database loading.
    public Donation() {
    }

    // Full constructor with all fields.
    public Donation(int donationId, String donor, double amount) {
        this.donationId = donationId;
        this.donor = donor;
        this.amount = amount;
    }

    // Returns the donationId of this donation.
    public int getDonationId() {
        return donationId;
    }

    // Sets the donationId of this donation.
    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    // Returns the donor of this donation.
    public String getDonor() {
        return donor;
    }

    // Sets the donor of this donation.
    public void setDonor(String donor) {
        this.donor = donor;
    }

    // Returns the amount of this donation.
    public double getAmount() {
        return amount;
    }

    // Sets the amount of this donation.
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Returns a text view of this donation.
    @Override
    public String toString() {
        return "Donation [donationId=" + donationId + ", donor=" + donor + ", amount=" + amount + "]";
    }
}
