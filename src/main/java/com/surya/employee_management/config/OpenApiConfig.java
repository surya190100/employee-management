package com.surya.employee_management.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Defines the HTTP Bearer JWT security scheme named "bearerAuth".
 * Protected controllers opt-in via {@code @SecurityRequirement(name = "bearerAuth")}.
 * Auth endpoints (/auth/register and /auth/login) are intentionally left without a
 * security requirement so they remain publicly accessible in Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Management System API",
                version = "1.0.0",
                description = "REST API for managing employees with JWT-based authentication and role-based access control.",
                contact = @Contact(
                        name = "Surya",
                        email = "surya@example.com"
                )
        ),
        servers = {
                @Server(url = "/", description = "Default server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Enter the JWT token obtained from POST /auth/login. " +
                "The token is automatically prefixed with 'Bearer ' by Swagger UI."
)
public class OpenApiConfig {
    // All configuration is driven by annotations above.
    // No bean registration needed — Springdoc picks up @OpenAPIDefinition and @SecurityScheme automatically.
}
