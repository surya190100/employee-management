package com.surya.employee_management.service;

import com.surya.employee_management.dto.CreateEmployeeRequest;
import com.surya.employee_management.dto.EmployeeResponse;
import com.surya.employee_management.dto.PageResponse;
import com.surya.employee_management.dto.UpdateEmployeeRequest;
import com.surya.employee_management.entity.Employee;
import com.surya.employee_management.exception.EmployeeEmailAlreadyExistsException;
import com.surya.employee_management.exception.EmployeeNotFoundException;
import com.surya.employee_management.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void findAllReturnsEmployeeResponses() {
        Employee employee = employee(1L, "jane@example.com");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<EmployeeResponse> employees = employeeService.findAll();

        assertEquals(1, employees.size());
        assertEquals(1L, employees.getFirst().employeeId());
        assertEquals("jane@example.com", employees.getFirst().email());
    }

    @Test
    void searchReturnsPageResponse() {
        Employee employee = employee(1L, "jane@example.com");
        Page<Employee> page = new PageImpl<>(List.of(employee), PageRequest.of(0, 10), 1);
        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<EmployeeResponse> response = employeeService.search(
                "Jane", "Engineering", "Developer", true, 0, 10, "employeeId", "asc");

        assertEquals(1, response.content().size());
        assertEquals(0, response.pageNumber());
        assertEquals(10, response.pageSize());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.last());
    }

    @Test
    void searchRejectsNegativePage() {
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.search(null, null, null, null, -1, 10, "employeeId", "asc"));
    }

    @Test
    void searchRejectsZeroSize() {
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.search(null, null, null, null, 0, 0, "employeeId", "asc"));
    }

    @Test
    void searchRejectsUnsupportedSortField() {
        assertThrows(IllegalArgumentException.class, () ->
                employeeService.search(null, null, null, null, 0, 10, "unknownField", "asc"));
    }

    @Test
    void findByIdThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.findById(42L));
    }

    @Test
    void createDefaultsActiveToTrueAndReturnsSavedEmployee() {
        CreateEmployeeRequest request = createRequest("jane@example.com", null);
        when(employeeRepository.existsByEmail(request.email())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setEmployeeId(7L);
            return saved;
        });

        EmployeeResponse response = employeeService.create(request);

        assertEquals(7L, response.employeeId());
        assertTrue(response.active());
        assertEquals(request.email(), response.email());
    }

    @Test
    void createRejectsDuplicateEmail() {
        CreateEmployeeRequest request = createRequest("jane@example.com", null);
        when(employeeRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmployeeEmailAlreadyExistsException.class, () -> employeeService.create(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateChangesEmployeeFields() {
        Employee employee = employee(4L, "old@example.com");
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Alex", "Smith", "alex@example.com", "555-0100", "Engineering", "Developer",
                new BigDecimal("90000.00"), LocalDate.of(2024, 1, 15), false);
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndEmployeeIdNot(request.email(), 4L)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        EmployeeResponse response = employeeService.update(4L, request);

        assertEquals("Alex", response.firstName());
        assertEquals("alex@example.com", response.email());
        assertFalse(response.active());
    }

    @Test
    void deleteThrowsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.delete(42L));
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    private static Employee employee(Long id, String email) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setEmail(email);
        employee.setPhoneNumber("555-0101");
        employee.setDepartment("Engineering");
        employee.setDesignation("Developer");
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setDateOfJoining(LocalDate.of(2022, 5, 1));
        return employee;
    }

    private static CreateEmployeeRequest createRequest(String email, Boolean active) {
        return new CreateEmployeeRequest(
                "Jane", "Doe", email, "555-0101", "Engineering", "Developer",
                new BigDecimal("85000.00"), LocalDate.of(2022, 5, 1), active);
    }
}
