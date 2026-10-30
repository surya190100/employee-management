package com.surya.employee_management.service;

import com.surya.employee_management.dto.CreateEmployeeRequest;
import com.surya.employee_management.dto.EmployeeResponse;
import com.surya.employee_management.dto.UpdateEmployeeRequest;
import com.surya.employee_management.entity.Employee;
import com.surya.employee_management.exception.EmployeeEmailAlreadyExistsException;
import com.surya.employee_management.exception.EmployeeNotFoundException;
import com.surya.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long employeeId) {
        return toResponse(findEmployee(employeeId));
    }

    @Override
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new EmployeeEmailAlreadyExistsException(request.email());
        }

        Employee employee = new Employee();
        apply(employee, request);
        if (request.active() != null) {
            employee.setActive(request.active());
        }
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long employeeId, UpdateEmployeeRequest request) {
        Employee employee = findEmployee(employeeId);
        if (employeeRepository.existsByEmailAndEmployeeIdNot(request.email(), employeeId)) {
            throw new EmployeeEmailAlreadyExistsException(request.email());
        }

        apply(employee, request);
        employee.setActive(request.active());
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(Long employeeId) {
        employeeRepository.delete(findEmployee(employeeId));
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    private static void apply(Employee employee, CreateEmployeeRequest request) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDepartment(request.department());
        employee.setDesignation(request.designation());
        employee.setSalary(request.salary());
        employee.setDateOfJoining(request.dateOfJoining());
    }

    private static void apply(Employee employee, UpdateEmployeeRequest request) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDepartment(request.department());
        employee.setDesignation(request.designation());
        employee.setSalary(request.salary());
        employee.setDateOfJoining(request.dateOfJoining());
    }

    private static EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getSalary(),
                employee.getDateOfJoining(),
                employee.isActive());
    }
}
