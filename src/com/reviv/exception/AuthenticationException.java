// src/com/reviv/exception/AuthenticationException.java
package com.reviv.exception;

public class AuthenticationException extends Exception {
    // Empty constructor for default message handling.
    public AuthenticationException() {
        super();
    }

    // Constructor with a custom message.
    public AuthenticationException(String message) {
        super(message);
    }
}
