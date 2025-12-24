# UI Wireframes & Component Design

## Page Structure

```
┌─────────────────────────────────────────────────────────────┐
│  Header (Logo, Nav, User Menu)                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                    Main Content Area                        │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Footer                                                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Pages Overview

| Page | Route | Description |
|------|-------|-------------|
| Landing | `/` | Hero section, features, CTA |
| Login | `/login` | Email/password form |
| Register | `/register` | Registration form |
| Dashboard | `/dashboard` | Account overview |
| Account Details | `/accounts/:id` | Single account view |
| Transfer | `/transfer` | Fund transfer form |
| Transactions | `/transactions` | Transaction history |

---

## Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Welcome, John                              [+ New Account] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  SAVINGS     │  │  CHECKING    │  │  Total       │      │
│  │  ****7890    │  │  ****4567    │  │  Balance     │      │
│  │  $5,000.00   │  │  $2,500.00   │  │  $7,500.00   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                             │
│  Quick Actions                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │  Transfer  │  │  History   │  │  Settings  │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│                                                             │
│  Recent Transactions                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  ↓ Deposit      +$500.00         Jan 15, 2024      │   │
│  │  ↑ Transfer     -$100.00         Jan 14, 2024      │   │
│  │  ↓ Deposit      +$1000.00        Jan 10, 2024      │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Hierarchy

```
App
├── AuthProvider
│   ├── PublicRoutes
│   │   ├── LandingPage
│   │   ├── LoginPage
│   │   └── RegisterPage
│   └── ProtectedRoutes
│       ├── DashboardPage
│       │   ├── AccountCard[]
│       │   ├── QuickActions
│       │   └── RecentTransactions
│       ├── AccountDetailsPage
│       │   ├── AccountInfo
│       │   └── TransactionList
│       ├── TransferPage
│       │   └── TransferForm
│       └── TransactionsPage
│           ├── FilterBar
│           └── TransactionTable
└── SharedComponents
    ├── Header
    ├── Footer
    ├── Button
    ├── Input
    ├── Card
    ├── Modal
    └── Toast
```

---

## Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Primary | `#2563EB` | Buttons, links |
| Success | `#16A34A` | Deposits, positive |
| Danger | `#DC2626` | Errors, withdrawals |
| Background | `#0F172A` | Dark mode bg |
| Surface | `#1E293B` | Cards, panels |
| Text | `#F8FAFC` | Primary text |
| Muted | `#94A3B8` | Secondary text |

---

## Responsive Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| Mobile | < 640px | Single column |
| Tablet | 640px - 1024px | 2 columns |
| Desktop | > 1024px | 3 columns |
