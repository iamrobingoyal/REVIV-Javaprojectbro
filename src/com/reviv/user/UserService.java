// src/com/reviv/user/UserService.java
package com.reviv.user;

import com.reviv.exception.AuthenticationException;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
import com.reviv.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserService {
    // Wrong passwords allowed before the account is locked.
    private static final int MAX_FAILED_ATTEMPTS = 3;
    // Role names that a person may choose while registering.
    private static final String ROLE_CITIZEN = "CITIZEN";
    private static final String ROLE_VOLUNTEER = "VOLUNTEER";
    // Failed login count for each username.
    private HashMap<String, Integer> failedLogins = new HashMap<String, Integer>();

    // Registers a citizen or volunteer and returns the saved user.
    public User registerUser(String username, String password, String role)
            throws InvalidInputException, DatabaseException {
        String cleanRole = checkRegistration(username, password, role);
        if (getUserByUsername(username) != null) {
            throw new InvalidInputException("Username is already taken");
        }
        // Password is stored as plain text because hashing is outside the syllabus.
        createUser(username, password, cleanRole);
        return getUserByUsername(username);
    }

    // Checks the registration data and returns the role in capital letters.
    private String checkRegistration(String username, String password, String role) throws InvalidInputException {
        if (username == null || username.trim().length() == 0) {
            throw new InvalidInputException("Username cannot be empty");
        }
        if (password == null || password.trim().length() == 0) {
            throw new InvalidInputException("Password cannot be empty");
        }
        String cleanRole = role.trim().toUpperCase();
        if (!cleanRole.equals(ROLE_CITIZEN) && !cleanRole.equals(ROLE_VOLUNTEER)) {
            throw new InvalidInputException("Only CITIZEN or VOLUNTEER can register");
        }
        return cleanRole;
    }

    // Logs in a user. Locks the account after 3 wrong passwords.
    public User loginUser(String username, String password) throws AuthenticationException, DatabaseException {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new AuthenticationException("User not found");
        }
        if (isUserLocked(username)) {
            throw new AuthenticationException("Account is locked after 3 wrong passwords");
        }
        if (!user.getPassword().equals(password)) {
            int count = recordFailedLogin(username);
            throw new AuthenticationException(buildWrongPasswordMessage(count));
        }
        resetFailedLogins(username);
        return user;
    }

    // Adds one failed attempt, saves it and locks the account at the limit.
    private int recordFailedLogin(String username) throws DatabaseException {
        int count = dbGetFailedLoginCount(username) + 1;
        failedLogins.put(username, count);
        updateLoginState(username, count, count >= MAX_FAILED_ATTEMPTS);
        return count;
    }

    // Builds the message shown after a wrong password.
    private String buildWrongPasswordMessage(int count) {
        if (count >= MAX_FAILED_ATTEMPTS) {
            return "Wrong password. Account is now locked";
        }
        return "Wrong password. Attempt " + count + " of " + MAX_FAILED_ATTEMPTS;
    }

    // Returns the failed login count (HashMap first, database the first time).
    public int getFailedLoginCount(String username) throws DatabaseException {
        Integer value = failedLogins.get(username);
        if (value == null) {
            return dbGetFailedLoginCount(username);
        }
        return value.intValue();
    }

    // Sets the failed login count back to zero after a good login.
    private void resetFailedLogins(String username) throws DatabaseException {
        if (dbGetFailedLoginCount(username) > 0) {
            updateLoginState(username, 0, false);
        }
        failedLogins.put(username, 0);
    }

    // Returns all users for the admin.
    public ArrayList<User> getAllUsers() throws DatabaseException {
        return dbGetAllUsers();
    }

    // Direct JDBC operations for user accounts.

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
    public int dbGetFailedLoginCount(String username) throws DatabaseException {
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
    public ArrayList<User> dbGetAllUsers() throws DatabaseException {
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
