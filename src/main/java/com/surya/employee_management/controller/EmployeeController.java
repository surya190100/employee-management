package com.surya.employee_management.controller;

import com.surya.employee_management.dto.CreateEmployeeRequest;
import com.surya.employee_management.dto.EmployeeResponse;
import com.surya.employee_management.dto.PageResponse;
import com.surya.employee_management.dto.UpdateEmployeeRequest;
import com.surya.employee_management.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "CRUD operations for employees. Requires JWT Bearer authentication.")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(
            summary = "Search and list employees",
            description = "Returns a paginated list of employees. Supports filtering by name, department, designation, and active status."
    )
    public PageResponse<EmployeeResponse> findAll(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "employeeId") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        return employeeService.search(name, department, designation, active, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public EmployeeResponse findById(@PathVariable("id") Long employeeId) {
        return employeeService.findById(employeeId);
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse created = employeeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.employeeId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public EmployeeResponse update(
            @PathVariable("id") Long employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return employeeService.update(employeeId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee by ID")
    public ResponseEntity<Void> delete(@PathVariable("id") Long employeeId) {
        employeeService.delete(employeeId);
        return ResponseEntity.noContent().build();
    }
}
