package com.eduverse.backend.exception;

// Thrown when a course has reached its enrollment seat limit
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(String message) {
        super(message);
    }
}
