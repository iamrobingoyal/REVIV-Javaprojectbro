// src/com/reviv/exception/ResourceUnavailableException.java
package com.reviv.exception;

public class ResourceUnavailableException extends Exception {
    // Empty constructor for default message handling.
    public ResourceUnavailableException() {
        super();
    }

    // Constructor with a custom message.
    public ResourceUnavailableException(String message) {
        super(message);
    }
}
