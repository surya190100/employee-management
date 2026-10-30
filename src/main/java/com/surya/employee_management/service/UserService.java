package com.surya.employee_management.service;

import com.surya.employee_management.dto.RegisterResponse;
import com.surya.employee_management.entity.User;
import com.surya.employee_management.entity.Role;
import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.exception.UsernameAlreadyExistsException;
import com.surya.employee_management.repository.RoleRepository;
import com.surya.employee_management.repository.UserRepository;
import com.surya.employee_management.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role is not configured."));
        UserRole assignment = new UserRole();
        assignment.setUser(savedUser);
        assignment.setRole(userRole);
        userRoleRepository.save(assignment);

        RegisterResponse response = new RegisterResponse();
        response.setUserId(savedUser.getUserId());
        response.setUsername(savedUser.getUsername());
        response.setEnabled(savedUser.isEnabled());
        return response;
    }
}
