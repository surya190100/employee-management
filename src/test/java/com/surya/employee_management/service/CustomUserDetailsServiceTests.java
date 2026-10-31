package com.surya.employee_management.service;

import com.surya.employee_management.entity.Role;
import com.surya.employee_management.entity.User;
import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.repository.RoleRepository;
import com.surya.employee_management.repository.UserRepository;
import com.surya.employee_management.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomUserDetailsServiceTests {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void loadUserByUsernameLoadsAllRolesViaJoinFetchWithoutLazyInitializationError() {
        User user = new User();
        user.setUsername("multi-role-user");
        user.setPassword("secret-hash");
        user.setEnabled(true);
        user = userRepository.save(user);

        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElseThrow();

        UserRole ur1 = new UserRole();
        ur1.setUser(user);
        ur1.setRole(userRole);
        userRoleRepository.save(ur1);

        UserRole ur2 = new UserRole();
        ur2.setUser(user);
        ur2.setRole(adminRole);
        userRoleRepository.save(ur2);

        // Fetch user roles using optimized repository method
        List<UserRole> assignedRoles = userRoleRepository.findAllWithRoleByUserId(user.getUserId());
        assertEquals(2, assignedRoles.size());

        // Call CustomUserDetailsService to load UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername("multi-role-user");

        assertEquals("multi-role-user", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertEquals(2, authorities.size());
    }

    @Test
    void loadUserByUsernameReflectsDisabledAccountStatus() {
        User disabledUser = new User();
        disabledUser.setUsername("disabled-account-user");
        disabledUser.setPassword("password-hash");
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername("disabled-account-user");
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("non-existent-user-12345"));
    }
}

