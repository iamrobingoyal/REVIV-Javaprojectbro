// src/com/reviv/service/ResourceService.java
package com.reviv.service;

import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.exception.ResourceUnavailableException;
import com.reviv.model.Resource;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceService {
    // Resource name and its quantity in stock.
    private HashMap<String, Integer> stock = new HashMap<String, Integer>();

    // Reloads the stock map from the database.
    public void loadResources() throws DatabaseException {
        stock.clear();
        ArrayList<Resource> list = dbGetAllResources();
        for (Resource r : list) {
            stock.put(r.getResourceName(), r.getQuantity());
        }
    }

    // Returns all resources (fresh from the database).
    public ArrayList<Resource> getAllResources() throws DatabaseException {
        return dbGetAllResources();
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
        deductQuantity(resourceName, quantity);
        stock.put(resourceName, getStock(resourceName) - quantity);
    }

    // Adds stock. A new resource name creates a new row.
    public void addStock(String resourceName, int quantity) throws InvalidInputException, DatabaseException {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than zero");
        }
        loadResources();
        if (stock.containsKey(resourceName)) {
            addQuantity(resourceName, quantity);
        } else {
            insertResource(resourceName, quantity);
        }
        stock.put(resourceName, getStock(resourceName) + quantity);
    }

    // Direct JDBC operations for resources.

    // Returns all resources from the database.
    public ArrayList<Resource> dbGetAllResources() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT resource_id, resource_name, quantity, used_quantity FROM resources ORDER BY resource_id");
            rs = ps.executeQuery();
            return toResourceList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load resources: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves a brand new resource with its first quantity.
    public void insertResource(String name, int quantity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO resources (resource_name, quantity, used_quantity) VALUES (?, ?, 0)");
            ps.setString(1, name);
            ps.setInt(2, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add resource: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Adds quantity to an existing resource.
    public void addQuantity(String name, int quantity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE resources SET quantity = quantity + ? WHERE resource_name = ?");
            ps.setInt(1, quantity);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add stock: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Takes quantity out of stock and adds it to the used counter.
    public int deductQuantity(String name, int quantity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE resources SET quantity = quantity - ?, used_quantity = used_quantity + ? WHERE resource_name = ? AND quantity >= ?");
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setString(3, name);
            ps.setInt(4, quantity);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to deduct stock: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Converts the resource result set into a list of resources.
    private ArrayList<Resource> toResourceList(ResultSet rs) throws SQLException {
        ArrayList<Resource> list = new ArrayList<Resource>();
        while (rs.next()) {
            list.add(new Resource(rs.getInt("resource_id"), rs.getString("resource_name"),
                    rs.getInt("quantity"), rs.getInt("used_quantity")));
        }
        return list;
    }

    // Closes result set, statement and connection safely.
    private void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close ResultSet");
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close PreparedStatement");
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close Connection");
        }
    }

}
