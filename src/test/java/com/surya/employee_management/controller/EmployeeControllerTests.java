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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
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

    @Value("${jwt.secret}")
    private String testJwtSecret;

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
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

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
    void unauthenticatedAndInvalidBearerRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/employees"));

        mockMvc.perform(get("/user/test"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer invalid-test-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The bearer token is invalid or expired."));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", bearer(expiredToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The bearer token is invalid or expired."));
    }

    @Test
    void swaggerUiAndOpenApiSpecArePublicAndDescribeBearerAuthorization() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/employees'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/auth/register'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist());
    }

    @Test
    void userAndAdminSecurityTestEndpointsEnforceRoles() throws Exception {
        mockMvc.perform(get("/user/test")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("employee-user"));

        mockMvc.perform(get("/admin/test")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/admin/test")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("employee-admin"));
    }

    @Test
    void duplicateEmployeeEmailReturnsConflict() throws Exception {
        employeeRepository.save(employee("duplicate@example.com"));

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEmployeeJson("duplicate@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
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

    @Test
    void paginationAndFilteringWorkWithCustomParameters() throws Exception {
        employeeRepository.deleteAll();

        for (int i = 1; i <= 15; i++) {
            Employee emp = new Employee();
            emp.setFirstName("Name" + i);
            emp.setLastName("Surname" + i);
            emp.setEmail("employee" + i + "@example.com");
            emp.setPhoneNumber("555-01" + String.format("%02d", i));
            emp.setDepartment(i % 2 == 0 ? "IT" : "HR");
            emp.setDesignation(i % 2 == 0 ? "Developer" : "Recruiter");
            emp.setSalary(new BigDecimal("50000.00"));
            emp.setDateOfJoining(LocalDate.of(2023, 1, i));
            emp.setActive(i <= 10);
            employeeRepository.save(emp);
        }

        // Default pagination: page 0, size 10
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));

        // Second page: page 1, size 10
        mockMvc.perform(get("/api/employees?page=1&size=10")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.last").value(true));

        // Filter by department = "IT" (7 even numbers between 1 and 15: 2,4,6,8,10,12,14)
        mockMvc.perform(get("/api/employees?department=IT")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7));

        // Filter by name (case-insensitive search "name1")
        mockMvc.perform(get("/api/employees?name=name1")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7)); // Name1, Name10, Name11, Name12, Name13, Name14, Name15

        // Filter by active status
        mockMvc.perform(get("/api/employees?active=true")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10));

        // Combined filter: department=IT and active=true (2, 4, 6, 8, 10 -> 5 employees)
        mockMvc.perform(get("/api/employees?department=IT&active=true")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void rejectsUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/employees?sortBy=unsupportedColumn")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid sortBy field")));
    }

    @Test
    void rejectsInvalidPageNumber() throws Exception {
        mockMvc.perform(get("/api/employees?page=-1")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero."));
    }

    @Test
    void rejectsNonNumericPageAndPageSizeAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/employees?page=not-a-number")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'page'."));

        mockMvc.perform(get("/api/employees?size=101")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100."));
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

    private String expiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testJwtSecret));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("employee-user")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
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
