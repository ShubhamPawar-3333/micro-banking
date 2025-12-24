# Phase 2: System Design

## Distributed Micro-Banking System

---

**Document Version:** 1.0  
**Date:** December 22, 2024  
**Author:** Development Team  
**Project:** Micro-Banking System  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [High-Level Architecture](#2-high-level-architecture)
3. [CQRS Pattern Implementation](#3-cqrs-pattern-implementation)
4. [Security Architecture](#4-security-architecture)
5. [Database Design](#5-database-design)
6. [API Design](#6-api-design)
7. [User Interface Design](#7-user-interface-design)
8. [Design Decisions](#8-design-decisions)
9. [Deliverables](#9-deliverables)

---

## 1. Executive Summary

This document details the system design phase of the Distributed Micro-Banking System. It covers the architectural decisions, database schema design, API contracts, and user interface specifications. The design emphasizes scalability, security, and maintainability while demonstrating senior-level engineering patterns.

The architecture follows a layered approach with clear separation of concerns:
- **Presentation Layer**: React SPA with component-based architecture
- **Application Layer**: Spring Boot REST API with service orchestration
- **Data Layer**: PostgreSQL with JPA/Hibernate ORM

---

## 2. High-Level Architecture

### 2.1 System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  React Frontend (Vite)                     │  │
│  │  Components │ Pages │ Hooks │ Services │ State Management  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API GATEWAY LAYER                          │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────────┐   │
│  │  CORS Filter  │  │  JWT Filter   │  │  Rate Limiting    │   │
│  └───────────────┘  └───────────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    CONTROLLERS                           │    │
│  │  AuthController │ AccountController │ TransferController │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     SERVICES                             │    │
│  │   AuthService   │  AccountService  │  TransferService   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   REPOSITORIES                           │    │
│  │  UserRepository │ AccountRepository │ TransactionRepo   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                                 │
│  ┌───────────────────────┐  ┌───────────────────────────────┐  │
│  │   PostgreSQL (Neon)   │  │    Redis (Upstash) - Cache    │  │
│  │   Users │ Accounts    │  │    Sessions │ Rate Limits     │  │
│  │   Transactions        │  │                               │  │
│  └───────────────────────┘  └───────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| **React Frontend** | User interface, form handling, API communication |
| **CORS Filter** | Cross-origin request validation |
| **JWT Filter** | Token extraction and validation |
| **Controllers** | Request handling, input validation, response formatting |
| **Services** | Business logic, transaction management |
| **Repositories** | Database operations, queries |
| **PostgreSQL** | Persistent data storage |
| **Redis** | Session caching, rate limiting |

---

## 3. CQRS Pattern Implementation

### 3.1 Command Query Responsibility Segregation

CQRS separates read and write operations into different models, allowing independent scaling and optimization.

```
┌─────────────────────────────────────────────────────────────────┐
│                       CQRS ARCHITECTURE                          │
├─────────────────────────────┬───────────────────────────────────┤
│       COMMAND SIDE          │          QUERY SIDE               │
│       (Write Model)         │          (Read Model)             │
├─────────────────────────────┼───────────────────────────────────┤
│                             │                                   │
│  ┌─────────────────────┐   │   ┌─────────────────────────┐     │
│  │ CreateAccountCommand│   │   │ GetAccountBalanceQuery  │     │
│  │ TransferFundsCommand│   │   │ GetTransactionHistory   │     │
│  │ CloseAccountCommand │   │   │ GetAccountDetailsQuery  │     │
│  └─────────────────────┘   │   └─────────────────────────┘     │
│            │               │              │                     │
│            ▼               │              ▼                     │
│  ┌─────────────────────┐   │   ┌─────────────────────────┐     │
│  │   Command Handlers  │   │   │    Query Handlers       │     │
│  │   (TransferService) │   │   │    (AccountService)     │     │
│  └─────────────────────┘   │   └─────────────────────────┘     │
│            │               │              │                     │
│            ▼               │              ▼                     │
│  ┌─────────────────────┐   │   ┌─────────────────────────┐     │
│  │    Event Store      │   │   │    Read Database        │     │
│  │  (Transaction Log)  │   │   │    (Optimized Views)    │     │
│  └─────────────────────┘   │   └─────────────────────────┘     │
│                             │                                   │
└─────────────────────────────┴───────────────────────────────────┘
```

### 3.2 Write Model Commands

| Command | Description | Service Method |
|---------|-------------|----------------|
| `CreateAccountCommand` | Create new bank account | `AccountService.createAccount()` |
| `TransferFundsCommand` | Transfer between accounts | `TransferService.transfer()` |
| `CloseAccountCommand` | Close existing account | `AccountService.closeAccount()` |

### 3.3 Read Model Queries

| Query | Description | Service Method |
|-------|-------------|----------------|
| `GetAccountsQuery` | List user's accounts | `AccountService.getAccountsByUserId()` |
| `GetBalanceQuery` | Get account balance | `AccountService.getAccountById()` |
| `GetTransactionsQuery` | Get transaction history | `TransferService.getTransactionsByUserId()` |

---

## 4. Security Architecture

### 4.1 Authentication Flow

```
┌─────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  User   │     │   Frontend  │     │   Backend   │     │   Database  │
└────┬────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                   │                   │
     │  1. Login Form  │                   │                   │
     │────────────────>│                   │                   │
     │                 │                   │                   │
     │                 │  2. POST /login   │                   │
     │                 │──────────────────>│                   │
     │                 │                   │                   │
     │                 │                   │  3. Verify User   │
     │                 │                   │──────────────────>│
     │                 │                   │                   │
     │                 │                   │  4. User Found    │
     │                 │                   │<──────────────────│
     │                 │                   │                   │
     │                 │                   │  5. Generate JWT  │
     │                 │                   │─────────┐         │
     │                 │                   │         │         │
     │                 │                   │<────────┘         │
     │                 │                   │                   │
     │                 │ 6. Return Tokens  │                   │
     │                 │<──────────────────│                   │
     │                 │                   │                   │
     │  7. Store Token │                   │                   │
     │<────────────────│                   │                   │
     │                 │                   │                   │
     │  8. API Request │ Bearer Token      │                   │
     │────────────────>│──────────────────>│                   │
     │                 │                   │                   │
     │                 │                   │  9. Validate JWT  │
     │                 │                   │─────────┐         │
     │                 │                   │<────────┘         │
     │                 │                   │                   │
     │                 │  10. Response     │                   │
     │<────────────────│<──────────────────│                   │
```

### 4.2 JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "user@example.com",
    "iat": 1703260800,
    "exp": 1703347200
  },
  "signature": "..."
}
```

### 4.3 Security Layers

| Layer | Implementation | Purpose |
|-------|----------------|---------|
| **Transport** | HTTPS/TLS | Encrypt data in transit |
| **Authentication** | JWT Tokens | Verify user identity |
| **Authorization** | @AuthenticationPrincipal | Restrict resource access |
| **Input Validation** | @Valid annotations | Prevent injection attacks |
| **Password Security** | BCrypt hashing | Secure credential storage |
| **CORS** | CorsConfigurationSource | Control cross-origin requests |

---

## 5. Database Design

### 5.1 Entity Relationship Diagram

```
┌────────────────────────┐       ┌────────────────────────┐
│         USERS          │       │        ACCOUNTS        │
├────────────────────────┤       ├────────────────────────┤
│ id (PK, UUID)          │       │ id (PK, UUID)          │
│ email (UK, NOT NULL)   │       │ user_id (FK)           │───┐
│ password_hash          │──1:N──│ account_number (UK)    │   │
│ first_name             │       │ account_type (ENUM)    │   │
│ last_name              │       │ balance (DECIMAL)      │   │
│ phone                  │       │ status (ENUM)          │   │
│ status (ENUM)          │       │ version (INT)          │   │
│ created_at             │       │ created_at             │   │
│ updated_at             │       │ updated_at             │   │
└────────────────────────┘       └────────────────────────┘   │
                                           │                   │
                                           │                   │
                                      1:N  │              1:N  │
                                           │                   │
                                           ▼                   ▼
                                 ┌────────────────────────────────┐
                                 │         TRANSACTIONS           │
                                 ├────────────────────────────────┤
                                 │ id (PK, UUID)                  │
                                 │ from_account_id (FK)           │
                                 │ to_account_id (FK)             │
                                 │ amount (DECIMAL)               │
                                 │ type (ENUM)                    │
                                 │ status (ENUM)                  │
                                 │ reference_number (UK)          │
                                 │ description                    │
                                 │ created_at                     │
                                 └────────────────────────────────┘
```

### 5.2 Table Specifications

#### 5.2.1 Users Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| email | VARCHAR(255) | UK, NOT NULL | User's email address |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| first_name | VARCHAR(100) | NOT NULL | User's first name |
| last_name | VARCHAR(100) | NOT NULL | User's last name |
| phone | VARCHAR(20) | NULLABLE | Contact phone number |
| status | ENUM | DEFAULT 'ACTIVE' | ACTIVE, SUSPENDED, CLOSED |
| created_at | TIMESTAMP | DEFAULT NOW() | Record creation time |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last modification time |

#### 5.2.2 Accounts Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID | FK → users.id | Account owner |
| account_number | VARCHAR(20) | UK, NOT NULL | 10-digit account number |
| account_type | ENUM | NOT NULL | SAVINGS, CHECKING |
| balance | DECIMAL(15,2) | DEFAULT 0.00 | Current balance |
| status | ENUM | DEFAULT 'ACTIVE' | ACTIVE, FROZEN, CLOSED |
| version | INTEGER | DEFAULT 0 | Optimistic lock version |
| created_at | TIMESTAMP | DEFAULT NOW() | Account creation time |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last modification time |

#### 5.2.3 Transactions Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| from_account_id | UUID | FK → accounts.id | Source account |
| to_account_id | UUID | FK → accounts.id | Destination account |
| amount | DECIMAL(15,2) | NOT NULL | Transaction amount |
| type | ENUM | NOT NULL | DEPOSIT, WITHDRAWAL, TRANSFER |
| status | ENUM | DEFAULT 'PENDING' | PENDING, COMPLETED, FAILED |
| reference_number | VARCHAR(50) | UK | Unique reference |
| description | TEXT | NULLABLE | Transaction description |
| created_at | TIMESTAMP | DEFAULT NOW() | Transaction time |

### 5.3 Indexing Strategy

```sql
-- Primary performance indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);

-- Lookup indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_accounts_number ON accounts(account_number);
```

---

## 6. API Design

### 6.1 API Overview

| Endpoint Group | Base Path | Authentication |
|----------------|-----------|----------------|
| Authentication | `/api/auth` | Public |
| Accounts | `/api/accounts` | Required |
| Transfers | `/api/transfers` | Required |
| Transactions | `/api/transactions` | Required |
| Health | `/api/health` | Public |

### 6.2 Authentication Endpoints

#### POST /api/auth/register
Creates a new user account.

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

#### POST /api/auth/login
Authenticates user and returns JWT tokens.

**Request:**
```json
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

### 6.3 Account Endpoints

#### GET /api/accounts
Returns all accounts for authenticated user.

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
      "createdAt": "2024-12-22T10:00:00Z"
    }
  ]
}
```

#### POST /api/accounts
Creates a new bank account.

**Request:**
```json
{
  "accountType": "SAVINGS"
}
```

### 6.4 Transfer Endpoints

#### POST /api/transfers
Transfers funds between accounts.

**Request:**
```json
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
  "amount": 100.00,
  "status": "COMPLETED",
  "createdAt": "2024-12-22T10:00:00Z"
}
```

### 6.5 Error Response Format

```json
{
  "error": {
    "code": "INSUFFICIENT_FUNDS",
    "message": "Account balance is insufficient for this transfer",
    "timestamp": "2024-12-22T10:00:00Z"
  }
}
```

---

## 7. User Interface Design

### 7.1 Page Structure

| Page | Route | Purpose |
|------|-------|---------|
| Landing | `/` | Marketing page, CTA |
| Login | `/login` | User authentication |
| Register | `/register` | New user registration |
| Dashboard | `/dashboard` | Account overview |
| Transfer | `/transfer` | Fund transfer form |
| Transactions | `/transactions` | Transaction history |

### 7.2 Component Hierarchy

```
App
├── AuthProvider (Context)
│   ├── PublicRoutes
│   │   ├── LandingPage
│   │   ├── LoginPage
│   │   └── RegisterPage
│   └── ProtectedRoutes
│       ├── Header
│       ├── DashboardPage
│       │   ├── AccountCard (multiple)
│       │   ├── QuickActions
│       │   └── RecentTransactions
│       ├── TransferPage
│       │   └── TransferForm
│       └── TransactionsPage
│           ├── FilterBar
│           └── TransactionList
└── Footer
```

### 7.3 Design System

#### Color Palette

| Name | Hex Code | Usage |
|------|----------|-------|
| Primary | #2563EB | Buttons, links, accents |
| Primary Dark | #1D4ED8 | Button hover states |
| Success | #16A34A | Positive amounts, deposits |
| Danger | #DC2626 | Errors, withdrawals |
| Background | #0F172A | Page background |
| Surface | #1E293B | Cards, panels |
| Text | #F8FAFC | Primary text |
| Muted | #94A3B8 | Secondary text |

#### Typography

| Element | Font | Size | Weight |
|---------|------|------|--------|
| Headings | Inter | 24-32px | 700 |
| Body | Inter | 16px | 400 |
| Labels | Inter | 14px | 500 |
| Small | Inter | 12px | 400 |

---

## 8. Design Decisions

### 8.1 Why CQRS?

| Decision | Rationale |
|----------|-----------|
| Separate read/write models | Allows independent optimization of queries vs commands |
| Scalability | Read replicas can be added without affecting writes |
| Audit trail | Commands create events that provide complete history |

### 8.2 Why Optimistic Locking?

| Decision | Rationale |
|----------|-----------|
| Version field on Account | Prevents lost updates in concurrent scenarios |
| No database-level locks | Better performance under normal conditions |
| Retry mechanism | Failed updates can be retried with fresh data |

### 8.3 Why JWT over Sessions?

| Decision | Rationale |
|----------|-----------|
| Stateless backend | No session storage required on server |
| Horizontal scaling | Any server can validate tokens |
| Mobile-friendly | Works well with non-browser clients |

---

## 9. Deliverables

### 9.1 Phase 2 Deliverables

| Deliverable | Status | Location |
|-------------|--------|----------|
| Architecture Diagram | ✅ Complete | `docs/architecture.md` |
| Database Schema (ERD) | ✅ Complete | `docs/database-schema.md` |
| API Contracts | ✅ Complete | `docs/api-contracts.md` |
| UI Wireframes | ✅ Complete | `docs/ui-wireframes.md` |
| Design Report | ✅ Complete | This document |

---

**End of Phase 2 Report**
