# API Contracts

## Base URL
```
Development: http://localhost:8080/api
Production:  https://your-domain.com/api
```

---

## Authentication Endpoints

### POST /auth/register
Register a new user.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecureP@ss123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

### POST /auth/login
Authenticate user and return tokens.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecureP@ss123"
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 86400
}
```

---

### POST /auth/refresh
Refresh access token.

**Request:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGc...",
  "expiresIn": 86400
}
```

---

## Account Endpoints

### GET /accounts
Get all accounts for authenticated user.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "accounts": [
    {
      "id": "uuid",
      "accountNumber": "1234567890",
      "accountType": "SAVINGS",
      "balance": 5000.00,
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

### POST /accounts
Create a new account.

**Headers:** `Authorization: Bearer {token}`

**Request:**
```json
{
  "accountType": "SAVINGS"
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "accountNumber": "1234567890",
  "accountType": "SAVINGS",
  "balance": 0.00,
  "status": "ACTIVE"
}
```

---

### GET /accounts/{accountId}
Get account details.

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "id": "uuid",
  "accountNumber": "1234567890",
  "accountType": "SAVINGS",
  "balance": 5000.00,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

---

### DELETE /accounts/{accountId}
Close an account (soft delete).

**Headers:** `Authorization: Bearer {token}`

**Response (200):**
```json
{
  "message": "Account closed successfully"
}
```

---

## Transfer Endpoints

### POST /transfers
Transfer funds between accounts.

**Headers:** `Authorization: Bearer {token}`

**Request:**
```json
{
  "fromAccountId": "uuid",
  "toAccountId": "uuid",
  "amount": 100.00,
  "description": "Payment for services"
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "referenceNumber": "TXN-2024-001",
  "fromAccountId": "uuid",
  "toAccountId": "uuid",
  "amount": 100.00,
  "status": "COMPLETED",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

## Transaction Endpoints

### GET /transactions
Get transaction history for user.

**Headers:** `Authorization: Bearer {token}`

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| accountId | UUID | Filter by account |
| page | int | Page number (default: 0) |
| size | int | Page size (default: 20) |
| startDate | ISO Date | Filter from date |
| endDate | ISO Date | Filter to date |

**Response (200):**
```json
{
  "content": [
    {
      "id": "uuid",
      "referenceNumber": "TXN-2024-001",
      "type": "TRANSFER",
      "amount": 100.00,
      "status": "COMPLETED",
      "description": "Payment",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

---

## Error Responses

All errors follow this format:

```json
{
  "error": {
    "code": "INSUFFICIENT_FUNDS",
    "message": "Account balance is insufficient for this transfer",
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | VALIDATION_ERROR | Invalid request body |
| 401 | UNAUTHORIZED | Missing or invalid token |
| 403 | FORBIDDEN | Access denied |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Duplicate resource |
| 422 | INSUFFICIENT_FUNDS | Not enough balance |
| 500 | INTERNAL_ERROR | Server error |
