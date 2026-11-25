// src/com/reviv/model/Volunteer.java
package com.reviv.model;

public class Volunteer {
    private int volunteerId;
    private String name;
    private String skill;
    private int userId;
    private boolean available;

    // Empty constructor for database loading.
    public Volunteer() {
    }

    // Full constructor with all fields.
    public Volunteer(int volunteerId, String name, String skill, int userId, boolean available) {
        this.volunteerId = volunteerId;
        this.name = name;
        this.skill = skill;
        this.userId = userId;
        this.available = available;
    }

    // Returns the volunteerId of this volunteer.
    public int getVolunteerId() {
        return volunteerId;
    }

    // Sets the volunteerId of this volunteer.
    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    // Returns the name of this volunteer.
    public String getName() {
        return name;
    }

    // Sets the name of this volunteer.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the skill of this volunteer.
    public String getSkill() {
        return skill;
    }

    // Sets the skill of this volunteer.
    public void setSkill(String skill) {
        this.skill = skill;
    }

    // Returns the userId of this volunteer.
    public int getUserId() {
        return userId;
    }

    // Sets the userId of this volunteer.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Returns the available of this volunteer.
    public boolean isAvailable() {
        return available;
    }

    // Sets the available of this volunteer.
    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Returns a text view of this volunteer.
    @Override
    public String toString() {
        return "Volunteer [volunteerId=" + volunteerId + ", name=" + name + ", skill=" + skill + ", userId=" + userId + ", available=" + available + "]";
    }
}
