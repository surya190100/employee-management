# Architecture

## Overview

This is a single Spring Boot application organized by responsibility. It keeps the existing MVC, service, repository, and JPA design; it does not use a frontend, microservices, or a separate persistence abstraction.

## Package responsibilities

| Package | Responsibility |
|---|---|
| `controller` | Maps HTTP routes to service calls, validates request DTOs, and chooses HTTP response semantics. |
| `service` | Implements registration, credential authentication, token creation support, user authority loading, and employee operations. |
| `repository` | Provides Spring Data JPA access; the user-role query fetches role data with one join to avoid lazy loading after the transaction. |
| `entity` | Maps users, roles, user-role assignments, and employees to the MySQL schema. |
| `dto` | Defines API input and output types independently of persistence entities. |
| `security` | Parses bearer JWTs per request and populates the Spring Security context. |
| `config` | Configures stateless authorization, CORS, BCrypt, role initialization, and OpenAPI metadata. |
| `exception` | Converts application and request errors to the common `ApiErrorResponse`; writes structured authentication/filter errors. |

## Request flows

### Registration and login

`AuthController` validates request DTOs and delegates registration to `UserService` or credential checks to `AuthService`. Registration stores a BCrypt password hash and assigns the seeded `USER` role. Login delegates authentication to Spring Security and returns a signed JWT with its configured lifetime.

### Protected API requests

`JwtAuthenticationFilter` validates the bearer token signature and expiry, loads current account details and authorities, and places the authenticated principal in the security context. `SecurityConfig` enforces roles and keeps the session policy stateless. Registration, login, and the Swagger/OpenAPI resources are public; other requests require authentication.

### Employee operations

`EmployeeController` accepts validated DTOs and delegates to `EmployeeService`. `EmployeeServiceImpl` validates pagination and allowlisted sort options, composes filters with a JPA `Specification`, and maps entities to response DTOs. Read operations are read-only transactional; writes use transactions.

## Persistence considerations

- `spring.jpa.open-in-view=false`; service transactions cover entity access and DTO mapping.
- Employee queries have no entity relationships to traverse, so their mapping does not trigger association N+1 queries.
- User authorities are loaded by a single fetch-join query in `UserRoleRepository`.
- The schema script is the database schema source for MySQL. Hibernate uses `validate`, not automatic schema mutation.
- The default role initializer creates role definitions only; it does not bootstrap an administrator account.

## Security boundaries

- Passwords use `BCryptPasswordEncoder`.
- JWT signing keys are supplied by `JWT_SECRET`; they are not stored in source.
- CORS origins are configurable and default to local development origins.
- Employee read access is granted to `USER` and `ADMIN`; mutations require `ADMIN`.
- The application has no public administrator provisioning endpoint. Provision administrator accounts through a separately controlled operational process.
