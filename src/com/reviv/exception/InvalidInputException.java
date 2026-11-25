// src/com/reviv/exception/InvalidInputException.java
package com.reviv.exception;

public class InvalidInputException extends Exception {
    // Empty constructor for default message handling.
    public InvalidInputException() {
        super();
    }

    // Constructor with a custom message.
    public InvalidInputException(String message) {
        super(message);
    }
}
