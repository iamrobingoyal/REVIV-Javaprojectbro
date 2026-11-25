// src/com/reviv/exception/TeamBusyException.java
package com.reviv.exception;

public class TeamBusyException extends Exception {
    // Empty constructor for default message handling.
    public TeamBusyException() {
        super();
    }

    // Constructor with a custom message.
    public TeamBusyException(String message) {
        super(message);
    }
}
