// src/com/reviv/model/Alert.java
package com.reviv.model;

public class Alert {
    private int alertId;
    private String sosId;
    private String zone;
    private String message;

    // Empty constructor for database loading.
    public Alert() {
    }

    // Full constructor with all fields.
    public Alert(int alertId, String sosId, String zone, String message) {
        this.alertId = alertId;
        this.sosId = sosId;
        this.zone = zone;
        this.message = message;
    }

    // Returns the alertId of this alert.
    public int getAlertId() {
        return alertId;
    }

    // Sets the alertId of this alert.
    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    // Returns the sosId of this alert.
    public String getSosId() {
        return sosId;
    }

    // Sets the sosId of this alert.
    public void setSosId(String sosId) {
        this.sosId = sosId;
    }

    // Returns the zone of this alert.
    public String getZone() {
        return zone;
    }

    // Sets the zone of this alert.
    public void setZone(String zone) {
        this.zone = zone;
    }

    // Returns the message of this alert.
    public String getMessage() {
        return message;
    }

    // Sets the message of this alert.
    public void setMessage(String message) {
        this.message = message;
    }

    // Returns a text view of this alert.
    @Override
    public String toString() {
        return "Alert [alertId=" + alertId + ", sosId=" + sosId + ", zone=" + zone + ", message=" + message + "]";
    }
}
