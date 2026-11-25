// src/com/reviv/user/UserService.java
package com.reviv.user;

import com.reviv.database.UserDAO;
import com.reviv.exception.AuthenticationException;
import com.reviv.exception.DatabaseException;
import com.reviv.exception.InvalidInputException;
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
    // The only DAO this service talks to.
    private UserDAO userDAO = new UserDAO();

    // Registers a citizen or volunteer and returns the saved user.
    public User registerUser(String username, String password, String role)
            throws InvalidInputException, DatabaseException {
        String cleanRole = checkRegistration(username, password, role);
        if (userDAO.getUserByUsername(username) != null) {
            throw new InvalidInputException("Username is already taken");
        }
        // Password is stored as plain text because hashing is outside the syllabus.
        userDAO.createUser(username, password, cleanRole);
        return userDAO.getUserByUsername(username);
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
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new AuthenticationException("User not found");
        }
        if (userDAO.isUserLocked(username)) {
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
        int count = getFailedLoginCount(username) + 1;
        failedLogins.put(username, count);
        userDAO.updateLoginState(username, count, count >= MAX_FAILED_ATTEMPTS);
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
            return userDAO.getFailedLoginCount(username);
        }
        return value.intValue();
    }

    // Sets the failed login count back to zero after a good login.
    private void resetFailedLogins(String username) throws DatabaseException {
        if (getFailedLoginCount(username) > 0) {
            userDAO.updateLoginState(username, 0, false);
        }
        failedLogins.put(username, 0);
    }

    // Returns all users for the admin.
    public ArrayList<User> getAllUsers() throws DatabaseException {
        return userDAO.getAllUsers();
    }
}
