# API documentation

Swagger UI is served at `/swagger-ui/index.html`; the OpenAPI document is at `/v3/api-docs`. Both are public. Protected operations use the HTTP Bearer scheme named `bearerAuth`.

## Authentication

### Register

`POST /auth/register` is public.

```json
{
  "username": "jane.doe",
  "password": "choose-a-long-password"
}
```

`201 Created` response:

```json
{
  "userId": 1,
  "username": "jane.doe",
  "enabled": true
}
```

Username must be 3–50 characters; password must be 6–100 characters. A duplicate username returns `409 Conflict`.

### Login

`POST /auth/login` is public.

```json
{
  "username": "jane.doe",
  "password": "choose-a-long-password"
}
```

`200 OK` response:

```json
{
  "accessToken": "<JWT returned by the API>",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```

Invalid credentials return `401 Unauthorized`. Do not include an actual token in documentation, logs, or issue reports.

## Employees

All employee endpoints require `Authorization: Bearer <accessToken>`.

### List, search, and filter

`GET /api/employees` is available to `USER` and `ADMIN`.

Supported query parameters:

| Parameter | Type | Behavior |
|---|---|---|
| `name` | string | Case-insensitive partial match against first or last name. |
| `department` | string | Case-insensitive exact match. |
| `designation` | string | Case-insensitive exact match. |
| `active` | boolean | Exact active-status filter. |
| `page` | integer | Zero-based page; defaults to `0`. |
| `size` | integer | 1–100 results per page; defaults to `10`. |
| `sortBy` | string | One of `employeeId`, `firstName`, `lastName`, `department`, `designation`, `dateOfJoining`; defaults to `employeeId`. |
| `direction` | string | `asc` or `desc`; defaults to `asc`. |

Example:

```text
GET /api/employees?name=jane&department=Engineering&active=true&page=0&size=10&sortBy=lastName&direction=asc
```

The response contains `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, and `last`.

### Read one

`GET /api/employees/{id}` is available to `USER` and `ADMIN`; it returns `200 OK` or `404 Not Found`.

### Create

`POST /api/employees` requires `ADMIN`.

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "555-0100",
  "department": "Engineering",
  "designation": "Developer",
  "salary": 85000.00,
  "dateOfJoining": "2024-01-15",
  "active": true
}
```

`active` is optional and defaults to `true`. A successful create returns `201 Created`, the resource body, and a `Location` header. Invalid input returns `400 Bad Request`; a duplicate email returns `409 Conflict`.

### Update

`PUT /api/employees/{id}` requires `ADMIN` and accepts the employee fields shown above. The `active` field is required for updates. Success returns `200 OK`; invalid data, duplicate email, and missing employee return `400`, `409`, and `404`, respectively.

### Delete

`DELETE /api/employees/{id}` requires `ADMIN`. Success returns `204 No Content`; a missing employee returns `404 Not Found`.

## Role test endpoints

- `GET /user/test`: requires `USER` or `ADMIN`.
- `GET /admin/test`: requires `ADMIN`.

## Error response

Application errors use this JSON shape:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: firstName: must not be blank",
  "path": "/api/employees"
}
```

The timestamp is generated per response. Authentication failures return `401`; insufficient role permissions return `403`. Invalid typed parameters, invalid pagination, and validation failures return `400`.
