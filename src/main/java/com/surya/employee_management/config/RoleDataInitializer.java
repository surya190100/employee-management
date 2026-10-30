package com.surya.employee_management.config;

import com.surya.employee_management.entity.Role;
import com.surya.employee_management.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleDataInitializer {

    @Bean
    public ApplicationRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            createRoleIfMissing(roleRepository, "USER");
            createRoleIfMissing(roleRepository, "ADMIN");
        };
    }

    private void createRoleIfMissing(RoleRepository roleRepository, String roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
        }
    }
}
