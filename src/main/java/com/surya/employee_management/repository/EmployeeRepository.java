package com.surya.employee_management.repository;

import com.surya.employee_management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndEmployeeIdNot(String email, Long employeeId);
}
