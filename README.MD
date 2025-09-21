# Todo Application - Microservices

A simple todo application built with Spring Boot microservices architecture.

## Architecture

```
Frontend → API Gateway (8080) → Auth Service (8081)
                              → Task Service (8082)
                              → MySQL Database
```

## Services

- **API Gateway (8080)**: Routes requests to other services
- **Auth Service (8081)**: User authentication with JWT
- **Task Service (8082)**: Task management (CRUD, categories, comments)

## Tech Stack

- Java 21 + Spring Boot 3.5.4
- MySQL 8.0
- JWT Authentication
- Docker & Docker Compose

## Quick Start

### With Docker (Recommended)
```bash
git clone <repository-url>
cd todo-app
docker-compose up -d
```

## URLs

- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Task Service: http://localhost:8082

## API Usage

### 1. Register User
```bash
POST /api/auth/register
{
  "username": "john",
  "email": "john@email.com", 
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 2. Login
```bash
POST /api/auth/login
{
  "usernameOrEmail": "john@email.com",
  "password": "password123"
}
```

### 3. Create Task (with JWT token)
```bash
POST /api/tasks
Authorization: Bearer <jwt-token>
{
  "title": "My Task",
  "description": "Task description",
  "priority": "HIGH"
}
```

### 4. Get Tasks
```bash
GET /api/tasks
Authorization: Bearer <jwt-token>
```

## Main Features

### Auth Service
- ✅ User registration/login
- ✅ JWT token generation
- ✅ Token refresh
- ✅ User profile management

### Task Service
- ✅ Create/Read/Update/Delete tasks
- ✅ Task categories and tags
- ✅ Comments on tasks
- ✅ Priority levels (LOW/MEDIUM/HIGH)
- ✅ Due date tracking
- ✅ Search tasks
- ✅ Task statistics

## Docker Commands

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs <service-name>

# Stop all services  
docker-compose down -v

# Rebuild services
docker-compose --build
```

## Configuration

Environment variables in `docker-compose.yml`:

```yaml
# Database
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/todo_auth
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: password

# JWT
JWT_SECRET: your-secret-key
JWT_ACCESS_TOKEN_EXPIRATION: 900000
JWT_REFRESH_TOKEN_EXPIRATION: 86400000
```

## Project Structure

```
todo-microservices/
├── api-gateway/          # Gateway service
├── auth-service/         # Authentication service  
├── task-service/         # Task management service
├── database/            # SQL init scripts
└── docker-compose.yml   # Docker orchestration
```
