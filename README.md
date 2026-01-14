# Pet Shop Management API

REST API developed in **Java with Spring Boot** to manage clients in a pet shop system.  
The project is focused on clean architecture, clear business rules, and a production-ready setup using **Docker**.

---

## 📌 Pre-requisites

To run this project you need:

- **Docker**
- **Docker Compose**

> No local Java or Maven installation is required when running with Docker.

---

## ▶️ Running the Project with Docker

From the project root directory:

```bash
docker compose up -d --build
```

This command will:
- Build the API image
- Start the PostgreSQL database
- Start the Spring Boot application

### Services & Ports

| Service | Port |
|------|------|
| API | `8080` |
| PostgreSQL | `5432` |

---

## 🗄️ Database Configuration

The application uses **PostgreSQL**.

When running with Docker, the database connection is configured via environment variables:

- **Host:** `postgres` (Docker service name)
- **Port:** `5432`
- **Database:** `petshop`
- **User:** `petshop_user`
- **Password:** `petshop_pass`

Data is persisted using a Docker volume, so it is not lost when containers restart.

---

## 📚 Swagger / OpenAPI Documentation

The API is documented using **Swagger (OpenAPI)**.

After starting the application, access:

```
http://localhost:8080/swagger-ui/index.html
```

Swagger UI allows you to:
- View all available endpoints
- Inspect request/response models
- Execute requests directly from the browser

The OpenAPI specification is available at:

```
http://localhost:8080/v3/api-docs
```

---

## 🔗 Available Endpoints

### Clients

| Method | Endpoint | Description |
|------|--------|------------|
| GET | `/clients` | List all active clients |
| GET | `/clients/{id}` | Get client by ID |
| GET | `/clients/cpf/{cpf}` | Get client by CPF |
| POST | `/clients` | Create a new client |
| PATCH | `/clients/cpf/{cpf}` | Update client contact info |
| DELETE | `/clients/cpf/{cpf}` | Inactivate (soft delete) client |
| PATCH | `/clients/cpf/{cpf}/activate` | Reactivate an inactive client |

---

## 📎 Example cURL Requests

### Create a Client
```bash
curl -X POST http://localhost:8080/clients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "cpf": "12345678909",
    "phone": "(11) 91234-5678",
    "email": "john.doe@email.com",
    "address": "Main Street, 123"
  }'
```

---

### List Active Clients
```bash
curl http://localhost:8080/clients
```

---

### Get Client by CPF
```bash
curl http://localhost:8080/clients/cpf/12345678909
```

---

### Update Client Contact Info (PATCH)
```bash
curl -X PATCH http://localhost:8080/clients/cpf/12345678909 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "new.email@email.com",
    "address": "New Address, 456"
  }'
```

---

### Inactivate Client (Soft Delete)
```bash
curl -X DELETE http://localhost:8080/clients/cpf/12345678909
```

---

### Reactivate Client
```bash
curl -X PATCH http://localhost:8080/clients/cpf/12345678909/activate
```

---

## 📝 Notes

- CPF can be sent **with or without formatting**
- CPF is stored normalized and returned formatted
- Only contact fields can be updated via PATCH
- Inactive clients cannot be updated (except reactivation)
- Errors are returned in a standardized JSON format

---

## 👤 Author

Backend portfolio project developed to demonstrate Java, Spring Boot, REST API design, Docker usage, and clean architecture practices.
