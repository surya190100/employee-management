package com.surya.employee_management.exception;

public class EmployeeEmailAlreadyExistsException extends RuntimeException {

    public EmployeeEmailAlreadyExistsException(String email) {
        super("An employee with email " + email + " already exists.");
    }
}
