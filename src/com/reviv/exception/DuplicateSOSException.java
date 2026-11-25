// src/com/reviv/exception/DuplicateSOSException.java
package com.reviv.exception;

public class DuplicateSOSException extends Exception {
    // Empty constructor for default message handling.
    public DuplicateSOSException() {
        super();
    }

    // Constructor with a custom message.
    public DuplicateSOSException(String message) {
        super(message);
    }
}
