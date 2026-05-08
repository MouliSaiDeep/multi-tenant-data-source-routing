# Multi-Tenant Spring Boot Starter with Data Source Routing

This project provides a reusable Spring Boot starter for header-based multi-tenant data source routing.

## Modules

- `multitenancy-spring-boot-starter`: The core library providing auto-configured multi-tenancy.
- `demo-application`: A sample application demonstrating the starter's functionality.

## Features

- **Header-based Routing**: Uses the `X-Tenant-ID` HTTP header to route database requests.
- **Dynamic Data Sources**: Automatically configures multiple data sources based on YAML configuration.
- **Data Isolation**: Ensures each tenant's data is kept in a separate database.
- **Context Management**: Thread-safe tenant context management using `ThreadLocal`.
- **Health Monitoring**: Custom actuator endpoint to monitor the health of all tenant data sources.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.x
- Docker and Docker Compose

### Build the Starter Locally

To build the starter and install it to your local Maven repository:

```bash
mvn clean install
```

### Run the Demo Application

You can run the entire environment (PostgreSQL + Demo App) using Docker Compose:

```bash
docker-compose up --build
```

The database initialization script (`db-init/init-tenant-dbs.sh`) will automatically create the following databases:
- `tenant1_db`
- `tenant2_db`
- `tenant3_db`

## API Endpoints

All endpoints require the `X-Tenant-ID` header (e.g., `tenant1`, `tenant2`, `tenant3`).

### User Management

- **Create User**: `POST /api/users`
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com"
  }
  ```
- **List Users**: `GET /api/users`
- **Get User by ID**: `GET /api/users/{id}`

### Health Checks

- **Overall Health**: `GET /actuator/health`
- **Tenant Data Sources Health**: `GET /actuator/health/datasources`

## Testing Data Isolation

1. Create a user for `tenant1`:
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -H "X-Tenant-ID: tenant1" \
     -d '{"name": "Tenant 1 User", "email": "user1@tenant1.com"}'
   ```

2. Create a user for `tenant2`:
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -H "X-Tenant-ID: tenant2" \
     -d '{"name": "Tenant 2 User", "email": "user2@tenant2.com"}'
   ```

3. Verify isolation:
   - Fetch users for `tenant1`:
     ```bash
     curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/users
     ```
     (Should only return the `tenant1` user)
   - Fetch users for `tenant2`:
     ```bash
     curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/users
     ```
     (Should only return the `tenant2` user)

## Error Handling

- **Missing Header**: Returns `400 Bad Request`.
- **Invalid Tenant ID**: Returns `404 Not Found`.
