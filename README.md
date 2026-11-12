# Employee Management System

A REST API for employee management built with Java 21 and Spring Boot. The application provides JWT authentication, role-based access control, employee CRUD operations, filtering, pagination, sorting, and OpenAPI documentation.

## Features

- Public user registration and login with BCrypt password hashing.
- Signed JWT bearer tokens and stateless Spring Security.
- `USER` and `ADMIN` role authorization.
- Employee create, read, update, and delete APIs.
- Case-insensitive employee-name search, department and designation filters, active-status filtering, pagination, and allowlisted sorting.
- Request validation and a consistent JSON error response.
- MySQL schema validation at application startup.
- OpenAPI documentation and Swagger UI.
- H2-backed automated tests that do not use a local MySQL database.

## Technology

Java 21, Spring Boot 4.1.1, Maven, Spring Data JPA, Hibernate, MySQL, Spring Security, JJWT 0.13.0, BCrypt, Bean Validation, Lombok, Springdoc OpenAPI, JUnit 5, Mockito, and H2.

## Architecture

The application uses a conventional layered structure:

- `controller`: REST endpoints and HTTP request/response mapping.
- `service`: authentication, registration, and employee business operations.
- `repository`: Spring Data persistence interfaces and queries.
- `entity`: JPA persistence models.
- `dto`: validated request and API response types.
- `security`: JWT request authentication.
- `config`: Spring Security, password encoder, role initialization, and OpenAPI configuration.
- `exception`: API error handling and security error responses.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the component responsibilities and request flow.

## Requirements

- Java 21.
- MySQL 8.0+ (the schema uses `utf8mb4_0900_ai_ci`).
- Windows PowerShell for the commands below, or another shell capable of running the Maven wrapper.

## Database setup

Create a MySQL database using the provided schema. From Windows Command Prompt:

```bat
mysql -u root -p < database\schema.sql
```

Alternatively, open `database/schema.sql` in MySQL Workbench and execute it. The script creates the `employee_management` database and its tables, keys, and indexes.

The application uses `spring.jpa.hibernate.ddl-auto=validate` by default. It checks that the database schema matches the JPA entities; it does not create or alter production tables. The application initializes the `USER` and `ADMIN` role rows on startup. It does not create an administrator account.

See [docs/DATABASE.md](docs/DATABASE.md) for the schema and operational notes.

## Configuration

Set these environment variables before starting the application:

| Variable | Required | Default | Description |
|---|---:|---|---|
| `DB_URL` | No | `jdbc:mysql://localhost:3306/employee_management` | JDBC URL for MySQL. |
| `DB_USERNAME` | No | `root` | MySQL username. |
| `DB_PASSWORD` | Yes for a password-protected MySQL account | Empty | MySQL password. |
| `JPA_DDL_AUTO` | No | `validate` | Hibernate schema action. Keep `validate` for normal use. |
| `JWT_SECRET` | Yes | Empty | Base64-encoded random key of at least 32 bytes for HS256 signing. |
| `JWT_EXPIRATION_MS` | No | `3600000` | JWT lifetime in milliseconds; must be positive. |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:3000,http://localhost:5173` | Comma-separated allowed browser origins. |

Generate a signing key in PowerShell (do not commit or share the generated value):

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Set database values in the same PowerShell session, using your local MySQL credentials:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/employee_management'
$env:DB_USERNAME = '<your-local-mysql-username>'
$env:DB_PASSWORD = '<your-local-mysql-password>'
```

Do not put real values in source control. The checked-in `.env.example` contains placeholders for the optional Compose workflow. Spring Boot does not load `.env` files automatically.

## Run on Windows

After MySQL is running, the schema is initialized, and the required environment variables are set:

```powershell
.\mvnw.cmd spring-boot:run
```

The application listens on port 8080 by default. Open Swagger UI at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html); the OpenAPI JSON is at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

## Authentication and authorization

1. Call `POST /auth/register` to create a `USER` account.
2. Call `POST /auth/login` with the account credentials. The response includes an `accessToken`.
3. In Swagger UI, select **Authorize** and enter the token. Swagger applies the `Bearer` scheme to protected operations.
4. Send `Authorization: Bearer <accessToken>` when calling protected APIs directly.

`USER` accounts can read employees but cannot create, update, or delete them. `ADMIN` accounts can perform employee CRUD. The application seeds the two role definitions only; administrator account provisioning is an operational task and is not exposed as a public API.

## API overview

| Method | Path | Access | Purpose |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a user; returns `201 Created`. |
| `POST` | `/auth/login` | Public | Authenticate and return a JWT. |
| `GET` | `/api/employees` | `USER`, `ADMIN` | Search/filter and return a page of employees. |
| `GET` | `/api/employees/{id}` | `USER`, `ADMIN` | Get one employee. |
| `POST` | `/api/employees` | `ADMIN` | Create an employee; returns `201 Created` and a `Location` header. |
| `PUT` | `/api/employees/{id}` | `ADMIN` | Replace employee details; returns `200 OK`. |
| `DELETE` | `/api/employees/{id}` | `ADMIN` | Delete an employee; returns `204 No Content`. |
| `GET` | `/user/test` | `USER`, `ADMIN` | Verify authenticated user access. |
| `GET` | `/admin/test` | `ADMIN` | Verify administrator access. |

Employee list query parameters are `name`, `department`, `designation`, `active`, `page`, `size`, `sortBy`, and `direction`. Page numbers are zero-based, size must be from 1 through 100, and sorting is restricted to `employeeId`, `firstName`, `lastName`, `department`, `designation`, and `dateOfJoining`; direction is `asc` or `desc`.

Request/response schemas and examples are in Swagger UI and [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md).

## Tests and build

The automated tests use the `test` profile and an in-memory H2 database; they do not connect to your configured MySQL server.

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Run the packaged application (after setting the environment variables above):

```powershell
java -jar .\target\employee-management-0.0.1-SNAPSHOT.jar
```

## Optional Docker Compose

The container workflow is optional and does not change local Maven development. Copy `.env.example` to `.env`, replace every placeholder (including a newly generated `JWT_SECRET`), then run:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

The API is available at `http://localhost:8080`. Compose starts MySQL and initializes the schema on the first creation of its data volume. Existing volumes are preserved; changing the init SQL does not migrate an already initialized database.

## Troubleshooting

- **JWT configuration error:** set `JWT_SECRET` to Base64 for at least 32 random bytes; plain text or a short value is not a suitable HS256 key.
- **Database connection or schema validation failure:** verify the MySQL service, JDBC URL, username/password, database name, and that `database/schema.sql` has been applied.
- **Port already in use:** stop the process using port 8080 or set `SERVER_PORT` to a free port when starting the application.
- **403 on employee mutation:** mutations require an `ADMIN` role; a registered account receives only `USER`.
- **Compose does not start:** make sure `.env` exists and all example placeholders have been replaced with local-only values.
