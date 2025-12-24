# Database Schema (ERD)

## Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ ACCOUNT : owns
    ACCOUNT ||--o{ TRANSACTION : has
    TRANSACTION }o--|| ACCOUNT : involves

    USER {
        uuid id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        string phone
        enum status "ACTIVE, SUSPENDED, CLOSED"
        timestamp created_at
        timestamp updated_at
    }

    ACCOUNT {
        uuid id PK
        uuid user_id FK
        string account_number UK
        enum account_type "SAVINGS, CHECKING"
        decimal balance
        enum status "ACTIVE, FROZEN, CLOSED"
        int version "Optimistic Locking"
        timestamp created_at
        timestamp updated_at
    }

    TRANSACTION {
        uuid id PK
        uuid from_account_id FK
        uuid to_account_id FK
        decimal amount
        enum type "DEPOSIT, WITHDRAWAL, TRANSFER"
        enum status "PENDING, COMPLETED, FAILED"
        string reference_number UK
        string description
        timestamp created_at
    }

    EVENT_STORE {
        uuid id PK
        string aggregate_type
        uuid aggregate_id
        string event_type
        json payload
        int sequence_number
        timestamp created_at
    }
```

---

## Table Definitions

### Users Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | |
| status | ENUM | DEFAULT 'ACTIVE' |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | DEFAULT NOW() |

### Accounts Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| user_id | UUID | FOREIGN KEY → users.id |
| account_number | VARCHAR(20) | UNIQUE, NOT NULL |
| account_type | ENUM | NOT NULL |
| balance | DECIMAL(15,2) | DEFAULT 0.00 |
| status | ENUM | DEFAULT 'ACTIVE' |
| version | INTEGER | DEFAULT 0 (Optimistic Lock) |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | DEFAULT NOW() |

### Transactions Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| from_account_id | UUID | FOREIGN KEY → accounts.id |
| to_account_id | UUID | FOREIGN KEY → accounts.id |
| amount | DECIMAL(15,2) | NOT NULL |
| type | ENUM | NOT NULL |
| status | ENUM | DEFAULT 'PENDING' |
| reference_number | VARCHAR(50) | UNIQUE |
| description | TEXT | |
| created_at | TIMESTAMP | DEFAULT NOW() |

### Event Store Table (for Event Sourcing)
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PRIMARY KEY |
| aggregate_type | VARCHAR(100) | NOT NULL |
| aggregate_id | UUID | NOT NULL |
| event_type | VARCHAR(100) | NOT NULL |
| payload | JSONB | NOT NULL |
| sequence_number | INTEGER | NOT NULL |
| created_at | TIMESTAMP | DEFAULT NOW() |

---

## Indexes

```sql
-- Performance indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_event_store_aggregate ON event_store(aggregate_type, aggregate_id);
```
