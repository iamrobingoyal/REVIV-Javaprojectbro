// src/com/reviv/model/SOSRequest.java
package com.reviv.model;

public class SOSRequest {
    private String sosId;
    private int userId;
    private String description;
    private String zone;
    private String status;
    private int teamId;

    // Empty constructor for database loading.
    public SOSRequest() {
    }

    // Full constructor with all fields.
    public SOSRequest(String sosId, int userId, String description, String zone, String status, int teamId) {
        this.sosId = sosId;
        this.userId = userId;
        this.description = description;
        this.zone = zone;
        this.status = status;
        this.teamId = teamId;
    }

    // Returns the sosId of this SOS request.
    public String getSosId() {
        return sosId;
    }

    // Sets the sosId of this SOS request.
    public void setSosId(String sosId) {
        this.sosId = sosId;
    }

    // Returns the userId of this SOS request.
    public int getUserId() {
        return userId;
    }

    // Sets the userId of this SOS request.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Returns the description of this SOS request.
    public String getDescription() {
        return description;
    }

    // Sets the description of this SOS request.
    public void setDescription(String description) {
        this.description = description;
    }

    // Returns the zone of this SOS request.
    public String getZone() {
        return zone;
    }

    // Sets the zone of this SOS request.
    public void setZone(String zone) {
        this.zone = zone;
    }

    // Returns the status of this SOS request.
    public String getStatus() {
        return status;
    }

    // Sets the status of this SOS request.
    public void setStatus(String status) {
        this.status = status;
    }

    // Returns the teamId of this SOS request.
    public int getTeamId() {
        return teamId;
    }

    // Sets the teamId of this SOS request.
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    // Returns a text view of this SOS request.
    @Override
    public String toString() {
        return "SOSRequest [sosId=" + sosId + ", userId=" + userId + ", description=" + description + ", zone=" + zone + ", status=" + status + ", teamId=" + teamId + "]";
    }
}
