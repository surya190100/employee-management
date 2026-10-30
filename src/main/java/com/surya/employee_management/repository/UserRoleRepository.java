package com.surya.employee_management.repository;

import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findAllByUser_UserId(Long userId);
}
