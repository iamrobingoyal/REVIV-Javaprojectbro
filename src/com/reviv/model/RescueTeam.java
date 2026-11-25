// src/com/reviv/model/RescueTeam.java
package com.reviv.model;

public class RescueTeam {
    private int teamId;
    private String teamName;
    private String captain;
    private boolean available;

    // Empty constructor for database loading.
    public RescueTeam() {
    }

    // Full constructor with all fields.
    public RescueTeam(int teamId, String teamName, String captain, boolean available) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.captain = captain;
        this.available = available;
    }

    // Returns the teamId of this rescue team.
    public int getTeamId() {
        return teamId;
    }

    // Sets the teamId of this rescue team.
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    // Returns the teamName of this rescue team.
    public String getTeamName() {
        return teamName;
    }

    // Sets the teamName of this rescue team.
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Returns the captain of this rescue team.
    public String getCaptain() {
        return captain;
    }

    // Sets the captain of this rescue team.
    public void setCaptain(String captain) {
        this.captain = captain;
    }

    // Returns true if the team is free.
    public boolean isAvailable() {
        return available;
    }

    // Sets the availability of this rescue team.
    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Returns a text view of this rescue team.
    @Override
    public String toString() {
        return "RescueTeam [teamId=" + teamId + ", teamName=" + teamName + ", captain=" + captain + ", available=" + available + "]";
    }
}
