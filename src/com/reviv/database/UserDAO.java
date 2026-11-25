// src/com/reviv/database/UserDAO.java
package com.reviv.database;

import com.reviv.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.reviv.user.User;

public class UserDAO {
    // Start of every user SELECT query.
    private static final String USER_COLUMNS = "SELECT id, username, password, role FROM users";

    // Saves a new user (plain password: hashing is outside the syllabus).
    public boolean createUser(String username, String password, String role) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create user: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Finds one user by username, or returns null when not found.
    public User getUserByUsername(String username) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(USER_COLUMNS + " WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            ArrayList<User> found = toUserList(rs);
            if (found.isEmpty()) {
                return null;
            }
            return found.get(0);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Returns the saved failed login count of a user.
    public int getFailedLoginCount(String username) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT failed_attempts FROM users WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("failed_attempts");
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read failed attempts: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Returns true when the user account is locked.
    public boolean isUserLocked(String username) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT locked FROM users WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next() && rs.getBoolean("locked");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read lock status: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Saves the failed login count and the lock flag of a user.
    public void updateLoginState(String username, int failed, boolean locked) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("UPDATE users SET failed_attempts = ?, locked = ? WHERE username = ?");
            ps.setInt(1, failed);
            ps.setBoolean(2, locked);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save login state: " + e.getMessage());
        } finally {
            closeResources(null, ps, conn);
        }
    }

    // Returns all users from the database.
    public ArrayList<User> getAllUsers() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(USER_COLUMNS + " ORDER BY id");
            rs = ps.executeQuery();
            return toUserList(rs);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load users: " + e.getMessage());
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // Converts the user result set into a list of users.
    private ArrayList<User> toUserList(ResultSet rs) throws SQLException {
        ArrayList<User> list = new ArrayList<User>();
        while (rs.next()) {
            list.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("role")));
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
