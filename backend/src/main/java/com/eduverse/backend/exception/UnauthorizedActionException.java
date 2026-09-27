package com.eduverse.backend.exception;

// Thrown when a user tries to do something they're not allowed to (e.g. review a course they never enrolled in)
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
