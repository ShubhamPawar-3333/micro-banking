# API Documentation

## Distributed Micro-Banking System

**Base URL:** `http://localhost:8080/api` (Development)  
**Production:** `https://your-backend.onrender.com/api`

---

## Authentication

All authenticated endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Endpoints

### Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecureP@ss123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-12-22T10:00:00Z"
}
```

---

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecureP@ss123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

---

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

---

### Accounts

#### Get All Accounts
```http
GET /accounts
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "accounts": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "accountNumber": "1234567890",
      "accountType": "SAVINGS",
      "balance": 5000.00,
      "status": "ACTIVE",
      "createdAt": "2024-12-22T10:00:00Z",
      "updatedAt": "2024-12-22T10:00:00Z"
    }
  ]
}
```

---

#### Get Account by ID
```http
GET /accounts/{accountId}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "1234567890",
  "accountType": "SAVINGS",
  "balance": 5000.00,
  "status": "ACTIVE",
  "createdAt": "2024-12-22T10:00:00Z",
  "updatedAt": "2024-12-22T10:00:00Z"
}
```

---

#### Create Account
```http
POST /accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "accountType": "SAVINGS"
}
```

**Account Types:** `SAVINGS`, `CHECKING`

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "9876543210",
  "accountType": "SAVINGS",
  "balance": 0.00,
  "status": "ACTIVE",
  "createdAt": "2024-12-22T10:00:00Z"
}
```

---

#### Close Account
```http
DELETE /accounts/{accountId}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Account closed successfully"
}
```

---

### Transfers

#### Transfer Funds
```http
POST /transfers
Authorization: Bearer {token}
Content-Type: application/json

{
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "toAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 100.00,
  "description": "Payment for services"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "referenceNumber": "TXN-2024-ABC12345",
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "toAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "amount": 100.00,
  "type": "TRANSFER",
  "status": "COMPLETED",
  "description": "Payment for services",
  "createdAt": "2024-12-22T10:00:00Z"
}
```

---

### Transactions

#### Get Transaction History
```http
GET /transactions
Authorization: Bearer {token}
```

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `accountId` | UUID | null | Filter by account |
| `page` | int | 0 | Page number |
| `size` | int | 20 | Page size |

**Example:**
```http
GET /transactions?accountId=550e8400...&page=0&size=10
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440003",
      "referenceNumber": "TXN-2024-ABC12345",
      "fromAccountId": "550e8400-e29b-41d4-a716-446655440001",
      "toAccountId": "550e8400-e29b-41d4-a716-446655440002",
      "amount": 100.00,
      "type": "TRANSFER",
      "status": "COMPLETED",
      "createdAt": "2024-12-22T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### Health

#### Health Check
```http
GET /health
```

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

---

## Error Responses

All errors follow this format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "timestamp": "2024-12-22T10:00:00Z"
  }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `NOT_FOUND` | 404 | Resource not found |
| `INSUFFICIENT_FUNDS` | 422 | Account balance too low |
| `VALIDATION_ERROR` | 400 | Invalid request body |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Rate Limits

| Endpoint | Limit |
|----------|-------|
| `/auth/*` | 10 req/min |
| `/transfers` | 30 req/min |
| Other | 100 req/min |

---

## Postman Collection

Import this URL into Postman:
```
https://your-repo.com/docs/MicroBanking.postman_collection.json
```
