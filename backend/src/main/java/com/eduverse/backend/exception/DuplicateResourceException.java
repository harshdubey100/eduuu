package com.eduverse.backend.exception;

// Used when a unique/one-time action is attempted twice (duplicate email, duplicate enrollment, etc.)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
