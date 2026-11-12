package com.surya.employee_management.service;

import com.surya.employee_management.dto.RegisterResponse;
import com.surya.employee_management.entity.Role;
import com.surya.employee_management.entity.User;
import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.exception.UsernameAlreadyExistsException;
import com.surya.employee_management.repository.RoleRepository;
import com.surya.employee_management.repository.UserRepository;
import com.surya.employee_management.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserHashesPasswordAndAssignsUserRole() {
        when(userRepository.existsByUsername("new-user")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserId(17L);
            return savedUser;
        });
        Role userRole = new Role();
        userRole.setRoleId(3L);
        userRole.setRoleName("USER");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));

        RegisterResponse response = userService.registerUser("new-user", "plain-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("bcrypt-hash", userCaptor.getValue().getPassword());
        assertEquals(17L, response.getUserId());
        assertEquals("new-user", response.getUsername());
        assertTrue(response.isEnabled());

        ArgumentCaptor<UserRole> assignmentCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(assignmentCaptor.capture());
        assertSame(userCaptor.getValue(), assignmentCaptor.getValue().getUser());
        assertSame(userRole, assignmentCaptor.getValue().getRole());
    }

    @Test
    void registerUserRejectsDuplicateUsernameBeforeHashingOrSaving() {
        when(userRepository.existsByUsername("existing-user")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.registerUser("existing-user", "plain-password"));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }
}
