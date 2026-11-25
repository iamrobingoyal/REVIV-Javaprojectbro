// src/com/reviv/service/ResourceService.java
package com.reviv.service;

import com.reviv.database.ResourceDAO;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.model.Resource;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceService {
    // Resource name and its quantity in stock.
    private HashMap<String, Integer> stock = new HashMap<String, Integer>();
    // DAO used to read and change stock in the database.
    private ResourceDAO resourceDAO = new ResourceDAO();

    // Reloads the stock map from the database.
    public void loadResources() throws DatabaseException {
        stock.clear();
        ArrayList<Resource> list = resourceDAO.getAllResources();
        for (Resource r : list) {
            stock.put(r.getResourceName(), r.getQuantity());
        }
    }

    // Returns all resources (fresh from the database).
    public ArrayList<Resource> getAllResources() throws DatabaseException {
        return resourceDAO.getAllResources();
    }

    // Returns the stock of one resource (0 if unknown).
    public int getStock(String resourceName) {
        Integer quantity = stock.get(resourceName);
        if (quantity == null) {
            return 0;
        }
        return quantity.intValue();
    }

    // Throws ResourceUnavailableException when the stock is too low.
    public void requireStock(String resourceName, int quantity) throws ResourceUnavailableException, DatabaseException {
        loadResources();
        if (getStock(resourceName) < quantity) {
            throw new ResourceUnavailableException("Not enough " + resourceName
                    + " (needed " + quantity + ", in stock " + getStock(resourceName) + ")");
        }
    }

    // Deducts stock in the database and in the map.
    public void deductResource(String resourceName, int quantity) throws ResourceUnavailableException, DatabaseException {
        requireStock(resourceName, quantity);
        resourceDAO.deductQuantity(resourceName, quantity);
        stock.put(resourceName, getStock(resourceName) - quantity);
    }

    // Adds stock. A new resource name creates a new row.
    public void addStock(String resourceName, int quantity) throws InvalidInputException, DatabaseException {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero");
        }
        loadResources();
        if (stock.containsKey(resourceName)) {
            resourceDAO.addQuantity(resourceName, quantity);
        } else {
            resourceDAO.insertResource(resourceName, quantity);
        }
        stock.put(resourceName, getStock(resourceName) + quantity);
    }
}
