package com.surya.employee_management.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("A user with username '" + username + "' already exists.");
    }
}
