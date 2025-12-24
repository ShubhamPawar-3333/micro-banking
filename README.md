# 🏦 Distributed Micro-Banking System

A production-ready full-stack banking application demonstrating **senior-level system design patterns** including CQRS, Event Sourcing, and distributed transaction management.

[![Backend CI](https://github.com/your-repo/micro-banking/actions/workflows/backend.yml/badge.svg)](https://github.com/your-repo/micro-banking/actions)
[![Frontend CI](https://github.com/your-repo/micro-banking/actions/workflows/frontend.yml/badge.svg)](https://github.com/your-repo/micro-banking/actions)

---

## 🚀 Features

| Feature | Description |
|---------|-------------|
| **User Authentication** | JWT-based auth with refresh tokens |
| **Account Management** | Create, view, and close bank accounts |
| **Fund Transfers** | Real-time transfers with optimistic locking |
| **Transaction History** | Paginated history with filtering |
| **Health Monitoring** | Actuator endpoints with Prometheus metrics |

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Runtime |
| Spring Boot | 3.4.x | Framework |
| Spring Security | 6.x | Authentication |
| PostgreSQL | 15.x | Database |
| JWT (jjwt) | 0.12.x | Token auth |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.x | UI Framework |
| Vite | 5.x | Build tool |
| React Query | 5.x | Server state |
| React Router | 6.x | Routing |

### Infrastructure
| Service | Provider | Free Tier |
|---------|----------|-----------|
| Backend | Render | 750 hrs/month |
| Frontend | Vercel | Unlimited |
| Database | Neon | 0.5 GB |

---

## 📁 Project Structure

```
Micro-Banking/
├── backend/
│   ├── src/main/java/com/microbanking/
│   │   ├── config/         # JWT, Security, Monitoring
│   │   ├── controller/     # REST endpoints
│   │   ├── dto/            # Request/Response objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Error handling
│   │   ├── repository/     # Data access
│   │   └── service/        # Business logic
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # UI components
│   │   ├── hooks/          # React hooks
│   │   ├── pages/          # Page components
│   │   └── services/       # API services
│   └── package.json
├── docs/
│   └── reports/            # Phase reports
└── .github/workflows/      # CI/CD
```

---

## ⚡ Quick Start

### Prerequisites
- Java 17+
- Node.js 20+
- PostgreSQL (or use Neon free tier)

### 1. Clone Repository
```bash
git clone https://github.com/your-username/micro-banking.git
cd micro-banking
```

### 2. Backend Setup
```bash
cd backend

# Configure environment
cp .env.example .env
# Edit .env with your database credentials

# Run with Maven
./mvnw spring-boot:run
```

### 3. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

### 4. Access Application
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

---

## 🔧 Environment Variables

### Backend (.env)
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/microbanking
DATABASE_USER=postgres
DATABASE_PASSWORD=your-password
JWT_SECRET=your-64-character-secret-key-here
CORS_ORIGINS=http://localhost:5173
```

### Frontend (.env)
```env
VITE_API_URL=http://localhost:8080/api
```

---

## 📚 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | Login, get tokens | ❌ |
| POST | `/api/auth/refresh` | Refresh access token | ❌ |
| GET | `/api/accounts` | List user accounts | ✅ |
| POST | `/api/accounts` | Create new account | ✅ |
| DELETE | `/api/accounts/{id}` | Close account | ✅ |
| POST | `/api/transfers` | Transfer funds | ✅ |
| GET | `/api/transactions` | Transaction history | ✅ |
| GET | `/api/health` | Health check | ❌ |

---

## 🏗️ System Design Highlights

### CQRS Pattern
```
Commands (Write)           Queries (Read)
┌─────────────────┐        ┌─────────────────┐
│ CreateAccount   │        │ GetAccounts     │
│ TransferFunds   │        │ GetBalance      │
│ CloseAccount    │        │ GetTransactions │
└────────┬────────┘        └────────┬────────┘
         │                          │
         ▼                          ▼
   Command Handler            Query Handler
         │                          │
         ▼                          ▼
   Event Store              Read Database
```

### Optimistic Locking
```java
@Entity
public class Account {
    @Version
    private Integer version; // Prevents concurrent updates
}
```

### JWT Security Flow
```
Client → Login → JWT Token → Bearer Header → Validated by Filter → Access Granted
```

---

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### Coverage Reports
```bash
# Backend
./mvnw test jacoco:report

# Frontend
npm run test:coverage
```

---

## 🚢 Deployment

### Automatic (CI/CD)
Push to `main` branch triggers automatic deployment:
1. Tests run on GitHub Actions
2. Backend deploys to Render
3. Frontend deploys to Vercel

### Manual Deployment
See [docs/reports/Phase_5_Deployment_Report.md](docs/reports/Phase_5_Deployment_Report.md) for detailed instructions.

---

## 📊 Monitoring

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/info` | App version info |

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [Phase 1: Planning](docs/reports/Phase_1_Planning_Report.md) | Requirements & scope |
| [Phase 2: Design](docs/reports/Phase_2_System_Design_Report.md) | Architecture & schema |
| [Phase 3: Development](docs/reports/Phase_3_Development_Report.md) | Implementation details |
| [Phase 4: Testing](docs/reports/Phase_4_Testing_Report.md) | Test strategy & coverage |
| [Phase 5: Deployment](docs/reports/Phase_5_Deployment_Report.md) | CI/CD & hosting |
| [Phase 6: Monitoring](docs/reports/Phase_6_Monitoring_Report.md) | Observability setup |

---

## 🎯 Senior-Level Skills Demonstrated

- **CQRS Pattern**: Separate read/write models
- **Optimistic Locking**: Concurrent transaction handling
- **JWT Authentication**: Stateless security
- **Repository Pattern**: Clean data access layer
- **DTO Pattern**: API contract separation
- **Global Exception Handling**: Consistent error responses
- **Request Tracing**: MDC-based correlation IDs
- **CI/CD Pipeline**: Automated testing & deployment
- **Infrastructure as Code**: Docker + GitHub Actions

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

**Your Name**
- Portfolio: [your-portfolio.com](https://your-portfolio.com)
- GitHub: [@your-username](https://github.com/your-username)
- LinkedIn: [your-linkedin](https://linkedin.com/in/your-username)
