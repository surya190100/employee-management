package com.surya.employee_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surya.employee_management.entity.Employee;
import com.surya.employee_management.entity.Role;
import com.surya.employee_management.entity.User;
import com.surya.employee_management.entity.UserRole;
import com.surya.employee_management.repository.EmployeeRepository;
import com.surya.employee_management.repository.RoleRepository;
import com.surya.employee_management.repository.UserRepository;
import com.surya.employee_management.repository.UserRoleRepository;
import com.surya.employee_management.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUpUsers() {
        userToken = tokenFor("employee-user", "USER");
        adminToken = tokenFor("employee-admin", "ADMIN");
    }

    @Test
    void userCanReadButCannotMutateEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String body = validEmployeeJson("user-created@example.com");
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/employees/1")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("user-updated@example.com")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/employees/1")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateUpdateAndDeleteEmployees() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("created@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.employeeId").isNumber())
                .andExpect(jsonPath("$.email").value("created@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        Employee existing = employeeRepository.save(employee("existing@example.com"));
        mockMvc.perform(put("/api/employees/" + existing.getEmployeeId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("updated@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        mockMvc.perform(delete("/api/employees/" + existing.getEmployeeId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void validationAndNotFoundResponsesAreReturned() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/employees/99999")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/employees/99999"));
    }

    @Test
    void employeeCanBeReadById() throws Exception {
        Employee employee = employeeRepository.save(employee("read@example.com"));

        mockMvc.perform(get("/api/employees/" + employee.getEmployeeId())
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employee.getEmployeeId()))
                .andExpect(jsonPath("$.email").value("read@example.com"));
    }

    private String tokenFor(String username, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("not-used-by-bearer-auth");
        user = userRepository.save(user);

        Role role = roleRepository.findByRoleName(roleName).orElseThrow();
        UserRole assignment = new UserRole();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleRepository.save(assignment);

        return jwtService.generateToken(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private String validEmployeeJson(String email) throws Exception {
        return objectMapper.writeValueAsString(new EmployeeRequest(
                "Taylor", "Jones", email, "555-0110", "Engineering", "Analyst",
                new BigDecimal("72000.00"), LocalDate.of(2023, 3, 20), true));
    }

    private static Employee employee(String email) {
        Employee employee = new Employee();
        employee.setFirstName("Casey");
        employee.setLastName("Rivera");
        employee.setEmail(email);
        employee.setPhoneNumber("555-0111");
        employee.setDepartment("Operations");
        employee.setDesignation("Coordinator");
        employee.setSalary(new BigDecimal("65000.00"));
        employee.setDateOfJoining(LocalDate.of(2021, 8, 10));
        return employee;
    }

    private record EmployeeRequest(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String department,
            String designation,
            BigDecimal salary,
            LocalDate dateOfJoining,
            boolean active) {
    }
}
