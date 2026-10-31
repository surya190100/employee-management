package com.surya.employee_management.repository;

import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findAllByUser_UserId(Long userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.userId = :userId")
    List<UserRole> findAllWithRoleByUserId(@Param("userId") Long userId);
}
