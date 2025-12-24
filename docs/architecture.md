# System Architecture

## High-Level Architecture

```mermaid
flowchart TB
    subgraph Client["Client Layer"]
        React["React Frontend<br/>(Vite + Tailwind)"]
    end

    subgraph Gateway["API Gateway"]
        CORS["CORS Filter"]
        Auth["JWT Auth Filter"]
    end

    subgraph Backend["Spring Boot Backend"]
        subgraph Controllers
            AuthCtrl["Auth Controller"]
            AccountCtrl["Account Controller"]
            TransferCtrl["Transfer Controller"]
            TxnCtrl["Transaction Controller"]
        end

        subgraph Services
            AuthSvc["Auth Service"]
            AccountSvc["Account Service"]
            TransferSvc["Transfer Service"]
            TxnSvc["Transaction Service"]
        end

        subgraph CQRS["CQRS Pattern"]
            Commands["Command Handlers<br/>(Write Operations)"]
            Queries["Query Handlers<br/>(Read Operations)"]
        end
    end

    subgraph Data["Data Layer"]
        Postgres["PostgreSQL<br/>(Neon)"]
        Redis["Redis<br/>(Upstash)"]
    end

    React --> CORS --> Auth --> Controllers
    Controllers --> Services
    Services --> Commands
    Services --> Queries
    Commands --> Postgres
    Queries --> Postgres
    Services --> Redis
```

---

## Component Breakdown

| Component | Responsibility |
|-----------|----------------|
| **Auth Controller** | Register, Login, Token Refresh |
| **Account Controller** | Create, View, Close Accounts |
| **Transfer Controller** | Initiate transfers between accounts |
| **Transaction Controller** | View transaction history |
| **Redis Cache** | Session tokens, Rate limiting |

---

## CQRS Implementation

```mermaid
flowchart LR
    subgraph Write["Command Side (Write)"]
        CMD["Command"] --> Handler["Command Handler"] --> Event["Event Store"]
        Event --> DB["PostgreSQL"]
    end

    subgraph Read["Query Side (Read)"]
        Query["Query"] --> QHandler["Query Handler"] --> View["Read Model"]
        View --> DB
    end
```

**Write Model (Commands):**
- `CreateAccountCommand`
- `TransferFundsCommand`
- `CloseAccountCommand`

**Read Model (Queries):**
- `GetAccountBalanceQuery`
- `GetTransactionHistoryQuery`
- `GetAccountDetailsQuery`

---

## Security Architecture

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant A as Auth Controller
    participant J as JWT Service
    participant D as Database

    U->>F: Login Request
    F->>A: POST /api/auth/login
    A->>D: Validate Credentials
    D-->>A: User Found
    A->>J: Generate JWT
    J-->>A: Access + Refresh Tokens
    A-->>F: Return Tokens
    F->>F: Store in Memory/HttpOnly Cookie
    F->>A: API Request + Bearer Token
    A->>J: Validate Token
    J-->>A: Valid
    A-->>F: Response Data
```
