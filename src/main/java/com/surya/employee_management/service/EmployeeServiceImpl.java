package com.surya.employee_management.service;

import com.surya.employee_management.dto.CreateEmployeeRequest;
import com.surya.employee_management.dto.EmployeeResponse;
import com.surya.employee_management.dto.PageResponse;
import com.surya.employee_management.dto.UpdateEmployeeRequest;
import com.surya.employee_management.entity.Employee;
import com.surya.employee_management.exception.EmployeeEmailAlreadyExistsException;
import com.surya.employee_management.exception.EmployeeNotFoundException;
import com.surya.employee_management.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Set<String> SUPPORTED_SORT_FIELDS = Set.of(
            "employeeId", "firstName", "lastName", "department", "designation", "dateOfJoining"
    );

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
    public PageResponse<EmployeeResponse> search(
            String name,
            String department,
            String designation,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        int clampedSize = Math.min(size, 100);

        String effectiveSortBy = (sortBy == null || sortBy.isBlank()) ? "employeeId" : sortBy;
        if (!SUPPORTED_SORT_FIELDS.contains(effectiveSortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: '" + effectiveSortBy
                    + "'. Supported fields are: " + String.join(", ", SUPPORTED_SORT_FIELDS));
        }

        Sort.Direction sortDirection;
        if (direction == null || direction.isBlank() || direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Invalid sort direction: '" + direction + "'. Supported directions are: asc, desc.");
        }

        Pageable pageable = PageRequest.of(page, clampedSize, Sort.by(sortDirection, effectiveSortBy));

        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isBlank()) {
                String pattern = "%" + name.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern)
                ));
            }

            if (department != null && !department.trim().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }

            if (designation != null && !designation.trim().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("designation")), designation.trim().toLowerCase()));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return PageResponse.from(employeePage.map(EmployeeServiceImpl::toResponse));
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
