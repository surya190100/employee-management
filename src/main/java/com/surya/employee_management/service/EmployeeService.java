package com.surya.employee_management.service;

import com.surya.employee_management.dto.CreateEmployeeRequest;
import com.surya.employee_management.dto.EmployeeResponse;
import com.surya.employee_management.dto.UpdateEmployeeRequest;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Long employeeId);

    EmployeeResponse create(CreateEmployeeRequest request);

    EmployeeResponse update(Long employeeId, UpdateEmployeeRequest request);

    void delete(Long employeeId);
}
