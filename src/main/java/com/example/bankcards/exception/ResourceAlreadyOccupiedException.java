package com.example.bankcards.exception;

public class ResourceAlreadyOccupiedException extends RuntimeException {
    public ResourceAlreadyOccupiedException(String message) {
        super(message);
    }
}
