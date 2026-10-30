package com.surya.employee_management.config;

import com.surya.employee_management.security.JwtAuthenticationFilter;
import com.surya.employee_management.exception.ApiErrorResponseWriter;
// import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final ApiErrorResponseWriter errorResponseWriter;

        public SecurityConfig(
                JwtAuthenticationFilter jwtAuthenticationFilter,
                ApiErrorResponseWriter errorResponseWriter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.errorResponseWriter = errorResponseWriter;
        }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> errorResponseWriter.write(
                                request, response, HttpStatus.UNAUTHORIZED,
                                "Authentication is required to access this resource."))
                        .accessDeniedHandler((request, response, exception) -> errorResponseWriter.write(
                                request, response, HttpStatus.FORBIDDEN,
                                "You do not have permission to access this resource.")))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/employees", "/api/employees/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/employees", "/api/employees/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/employees", "/api/employees/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees", "/api/employees/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
