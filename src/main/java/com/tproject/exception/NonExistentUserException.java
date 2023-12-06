package com.tproject.exception;

public class NonExistentUserException extends NonExistentEntityException {

    public NonExistentUserException() {
        super("User does not exist");
    }
}
