package com.tproject.exception;

public class NonExistentEntityException extends RuntimeException {

    public NonExistentEntityException(String message) {
        super(message);
    }
}
