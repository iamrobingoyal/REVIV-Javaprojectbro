// src/com/reviv/model/Shelter.java
package com.reviv.model;

public class Shelter {
    private int shelterId;
    private String shelterName;
    private String zone;
    private int capacity;
    private int freeCapacity;

    // Empty constructor for database loading.
    public Shelter() {
    }

    // Full constructor with all fields.
    public Shelter(int shelterId, String shelterName, String zone, int capacity, int freeCapacity) {
        this.shelterId = shelterId;
        this.shelterName = shelterName;
        this.zone = zone;
        this.capacity = capacity;
        this.freeCapacity = freeCapacity;
    }

    // Returns the shelterId of this shelter.
    public int getShelterId() {
        return shelterId;
    }

    // Sets the shelterId of this shelter.
    public void setShelterId(int shelterId) {
        this.shelterId = shelterId;
    }

    // Returns the shelterName of this shelter.
    public String getShelterName() {
        return shelterName;
    }

    // Sets the shelterName of this shelter.
    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    // Returns the zone of this shelter.
    public String getZone() {
        return zone;
    }

    // Sets the zone of this shelter.
    public void setZone(String zone) {
        this.zone = zone;
    }

    // Returns the capacity of this shelter.
    public int getCapacity() {
        return capacity;
    }

    // Sets the capacity of this shelter.
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Returns the freeCapacity of this shelter.
    public int getFreeCapacity() {
        return freeCapacity;
    }

    // Sets the freeCapacity of this shelter.
    public void setFreeCapacity(int freeCapacity) {
        this.freeCapacity = freeCapacity;
    }

    // Returns a text view of this shelter.
    @Override
    public String toString() {
        return "Shelter [shelterId=" + shelterId + ", shelterName=" + shelterName + ", zone=" + zone + ", capacity=" + capacity + ", freeCapacity=" + freeCapacity + "]";
    }
}
