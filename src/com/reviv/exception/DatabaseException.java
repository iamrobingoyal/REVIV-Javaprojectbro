// src/com/reviv/exception/DatabaseException.java
package com.reviv.exception;

public class DatabaseException extends Exception {
    // Empty constructor for default message handling.
    public DatabaseException() {
        super();
    }

    // Constructor with a custom message.
    public DatabaseException(String message) {
        super(message);
    }
}
