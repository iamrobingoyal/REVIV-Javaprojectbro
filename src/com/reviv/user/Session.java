// src/com/reviv/user/Session.java
package com.reviv.user;

public class Session {
    // Holds the currently logged in user.
    private User currentUser;
    // Holds the login status flag.
    private boolean loggedIn = false;

    // Returns true when a user is logged in.
    public boolean isLoggedIn() {
        return loggedIn;
    }

    // Returns the current user.
    public User getCurrentUser() {
        return currentUser;
    }

    // Sets the current user and the login status.
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.loggedIn = true;
    }

    // Logs out the current user.
    public void logout() {
        this.currentUser = null;
        this.loggedIn = false;
    }
}
