// src/com/reviv/model/Hospital.java
package com.reviv.model;

public class Hospital {
    private int hospitalId;
    private String hospitalName;
    private String zone;
    private int freeBeds;

    // Empty constructor for database loading.
    public Hospital() {
    }

    // Full constructor with all fields.
    public Hospital(int hospitalId, String hospitalName, String zone, int freeBeds) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.zone = zone;
        this.freeBeds = freeBeds;
    }

    // Returns the hospitalId of this hospital.
    public int getHospitalId() {
        return hospitalId;
    }

    // Sets the hospitalId of this hospital.
    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    // Returns the hospitalName of this hospital.
    public String getHospitalName() {
        return hospitalName;
    }

    // Sets the hospitalName of this hospital.
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    // Returns the zone of this hospital.
    public String getZone() {
        return zone;
    }

    // Sets the zone of this hospital.
    public void setZone(String zone) {
        this.zone = zone;
    }

    // Returns the freeBeds of this hospital.
    public int getFreeBeds() {
        return freeBeds;
    }

    // Sets the freeBeds of this hospital.
    public void setFreeBeds(int freeBeds) {
        this.freeBeds = freeBeds;
    }

    // Returns a text view of this hospital.
    @Override
    public String toString() {
        return "Hospital [hospitalId=" + hospitalId + ", hospitalName=" + hospitalName + ", zone=" + zone + ", freeBeds=" + freeBeds + "]";
    }
}
