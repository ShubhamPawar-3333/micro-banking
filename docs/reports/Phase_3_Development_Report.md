# Phase 3: Development

## Distributed Micro-Banking System

---

**Document Version:** 1.0  
**Date:** December 22, 2024  
**Author:** Development Team  
**Project:** Micro-Banking System  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Backend Implementation](#2-backend-implementation)
3. [Frontend Implementation](#3-frontend-implementation)
4. [Code Quality Standards](#4-code-quality-standards)
5. [Deliverables](#5-deliverables)

---

## 1. Executive Summary

This document details the development phase of the Distributed Micro-Banking System. A total of **32 source files** were created across the backend (22 files) and frontend (10 files), implementing the complete banking functionality as specified in the design phase.

### Development Statistics

| Metric | Backend | Frontend | Total |
|--------|---------|----------|-------|
| Files Created | 22 | 10 | 32 |
| Lines of Code | ~1,500 | ~1,000 | ~2,500 |
| Classes/Components | 19 | 8 | 27 |

---

## 2. Backend Implementation

### 2.1 Package Structure

```
com.microbanking/
├── MicroBankingApplication.java    # Main entry point
├── config/
│   ├── JwtUtil.java               # JWT generation/validation
│   ├── JwtAuthFilter.java         # Request authentication filter
│   └── SecurityConfig.java        # Security configuration
├── controller/
│   ├── AuthController.java        # Authentication endpoints
│   ├── AccountController.java     # Account management endpoints
│   ├── TransferController.java    # Transfer/transaction endpoints
│   └── HealthController.java      # Health check endpoint
├── dto/
│   ├── RegisterRequest.java       # Registration input
│   ├── LoginRequest.java          # Login credentials
│   ├── AuthResponse.java          # Token response
│   ├── AccountDto.java            # Account data transfer
│   ├── TransferRequest.java       # Transfer input
│   └── TransactionDto.java        # Transaction data transfer
├── entity/
│   ├── User.java                  # User entity with status
│   ├── Account.java               # Account with optimistic lock
│   └── Transaction.java           # Transaction record
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── InsufficientFundsException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   └── TransactionRepository.java
└── service/
    ├── AuthService.java           # Authentication logic
    ├── AccountService.java        # Account business logic
    └── TransferService.java       # Transfer business logic
```

### 2.2 Entity Implementation

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts;
}
```

**Key Features:**
- UUID primary key for distributed systems
- Email uniqueness constraint
- Status enum (ACTIVE, SUSPENDED, CLOSED)
- One-to-many relationship with accounts

#### Account Entity
```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String accountNumber;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Version
    private Integer version;  // Optimistic locking
}
```

**Key Features:**
- BigDecimal for precise monetary calculations
- @Version annotation for optimistic locking
- Auto-generated unique account numbers

### 2.3 Service Layer Implementation

#### TransferService - Fund Transfer Logic
```java
@Transactional
public TransactionDto transfer(TransferRequest request, UUID userId) {
    // 1. Acquire locks on both accounts
    Account fromAccount = accountRepository.findByIdWithLock(request.getFromAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
    
    Account toAccount = accountRepository.findByIdWithLock(request.getToAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));

    // 2. Validate ownership
    if (!fromAccount.getUser().getId().equals(userId)) {
        throw new SecurityException("Access denied");
    }

    // 3. Validate balance
    if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
        throw new InsufficientFundsException("Insufficient funds");
    }

    // 4. Perform atomic transfer
    fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
    toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

    // 5. Create transaction record
    Transaction transaction = Transaction.builder()
            .fromAccount(fromAccount)
            .toAccount(toAccount)
            .amount(request.getAmount())
            .type(TransactionType.TRANSFER)
            .status(TransactionStatus.COMPLETED)
            .referenceNumber(generateReferenceNumber())
            .build();

    return toDto(transactionRepository.save(transaction));
}
```

**Key Features:**
- @Transactional for ACID compliance
- Optimistic locking via findByIdWithLock
- Ownership validation before transfer
- Atomic balance updates

### 2.4 Security Implementation

#### JWT Configuration
```java
@Component
public class JwtUtil {
    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}
```

#### Security Filter Chain
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

---

## 3. Frontend Implementation

### 3.1 Component Structure

```
src/
├── App.jsx                 # Main app with routing
├── main.jsx               # Entry point
├── index.css              # Global styles
├── components/
│   └── Header.jsx         # Navigation header
├── hooks/
│   └── useAuth.jsx        # Auth context provider
├── pages/
│   ├── LoginPage.jsx      # Login form
│   ├── RegisterPage.jsx   # Registration form
│   ├── DashboardPage.jsx  # Account overview
│   └── TransferPage.jsx   # Fund transfer
└── services/
    ├── api.js             # Axios configuration
    └── index.js           # Service exports
```

### 3.2 State Management

#### AuthContext Implementation
```jsx
export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(
    authService.isAuthenticated()
  );
  
  const login = async (credentials) => {
    await authService.login(credentials);
    setIsAuthenticated(true);
  };

  const logout = () => {
    authService.logout();
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
```

### 3.3 API Integration

#### Axios Client with Interceptors
```javascript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor - add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 3.4 Design System

#### CSS Variables
```css
:root {
  --color-primary: #2563eb;
  --color-success: #16a34a;
  --color-danger: #dc2626;
  --color-bg: #0f172a;
  --color-surface: #1e293b;
  --color-text: #f8fafc;
  --color-muted: #94a3b8;
}
```

**Implemented Styles:**
- Dark theme with modern aesthetic
- Card-based layouts
- Form components with validation states
- Responsive breakpoints (mobile, tablet, desktop)
- Button variants (primary, secondary, danger)
- Modal overlay system

---

## 4. Code Quality Standards

### 4.1 Backend Standards

| Standard | Implementation |
|----------|----------------|
| **Naming** | PascalCase classes, camelCase methods |
| **Validation** | Jakarta validation annotations |
| **Exception Handling** | Global exception handler |
| **Logging** | SLF4J with structured messages |
| **Documentation** | JavaDoc on public methods |

### 4.2 Frontend Standards

| Standard | Implementation |
|----------|----------------|
| **Naming** | PascalCase components, camelCase functions |
| **State** | React hooks, context for global state |
| **Styling** | CSS custom properties, BEM-like naming |
| **Error Handling** | Try-catch in async functions |

---

## 5. Deliverables

### 5.1 Backend Files (22)

| Category | Files |
|----------|-------|
| Entities | User.java, Account.java, Transaction.java |
| Repositories | UserRepository.java, AccountRepository.java, TransactionRepository.java |
| DTOs | RegisterRequest.java, LoginRequest.java, AuthResponse.java, AccountDto.java, TransferRequest.java, TransactionDto.java |
| Services | AuthService.java, AccountService.java, TransferService.java |
| Controllers | AuthController.java, AccountController.java, TransferController.java, HealthController.java |
| Config | JwtUtil.java, JwtAuthFilter.java, SecurityConfig.java |
| Exceptions | ResourceNotFoundException.java, InsufficientFundsException.java, GlobalExceptionHandler.java |

### 5.2 Frontend Files (10)

| Category | Files |
|----------|-------|
| Core | App.jsx, main.jsx, index.css |
| Components | Header.jsx |
| Hooks | useAuth.jsx |
| Pages | LoginPage.jsx, RegisterPage.jsx, DashboardPage.jsx, TransferPage.jsx |
| Services | api.js, index.js |

---

**End of Phase 3 Report**
