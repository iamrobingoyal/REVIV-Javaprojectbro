// src/com/reviv/user/User.java
package com.reviv.user;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;

    // Empty constructor for database loading.
    public User() {
    }

    // Full constructor with all fields.
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Returns the id of this user.
    public int getId() {
        return id;
    }

    // Sets the id of this user.
    public void setId(int id) {
        this.id = id;
    }

    // Returns the username of this user.
    public String getUsername() {
        return username;
    }

    // Sets the username of this user.
    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the password of this user.
    public String getPassword() {
        return password;
    }

    // Sets the password of this user.
    public void setPassword(String password) {
        this.password = password;
    }

    // Returns the role of this user.
    public String getRole() {
        return role;
    }

    // Sets the role of this user.
    public void setRole(String role) {
        this.role = role;
    }

    // Returns a text view of this user.
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", role=" + role + "]";
    }
}
