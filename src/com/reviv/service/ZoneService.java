// src/com/reviv/service/ZoneService.java
package com.reviv.service;

import java.util.ArrayList;
import java.util.HashSet;

public class ZoneService {
    // Unique affected zones (a HashSet never keeps duplicates).
    private HashSet<String> zones = new HashSet<String>();

    // Adds a zone to the affected zone set.
    public void addZone(String zone) {
        zones.add(zone);
    }

    // Returns true when the given zone is already affected.
    public boolean isZoneAffected(String zone) {
        return zones.contains(zone);
    }

    // Returns all affected zones as a list.
    public ArrayList<String> getAllZones() {
        return new ArrayList<String>(zones);
    }
}
