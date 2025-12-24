# Phase 5: Deployment

## Distributed Micro-Banking System

---

**Document Version:** 1.0  
**Date:** December 22, 2024  
**Author:** Development Team  
**Project:** Micro-Banking System  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Infrastructure Architecture](#2-infrastructure-architecture)
3. [CI/CD Pipeline](#3-cicd-pipeline)
4. [Deployment Configuration](#4-deployment-configuration)
5. [Environment Setup](#5-environment-setup)
6. [Deployment Procedures](#6-deployment-procedures)
7. [Deliverables](#7-deliverables)

---

## 1. Executive Summary

This document details the deployment phase of the Distributed Micro-Banking System. The deployment strategy utilizes free-tier cloud services to demonstrate professional DevOps practices while maintaining zero infrastructure costs.

### Deployment Overview

| Component | Platform | Free Tier Limits |
|-----------|----------|------------------|
| Backend API | Render | 750 hrs/month |
| Frontend | Vercel | Unlimited |
| Database | Neon | 0.5 GB storage |
| CI/CD | GitHub Actions | 2000 mins/month |

---

## 2. Infrastructure Architecture

### 2.1 Production Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              INTERNET                                    │
└───────────────────────────────────┬─────────────────────────────────────┘
                                    │
        ┌───────────────────────────┴───────────────────────────┐
        │                                                        │
        ▼                                                        ▼
┌───────────────────────┐                          ┌───────────────────────┐
│     VERCEL CDN        │                          │      RENDER           │
│   (Edge Network)      │                          │   (Web Service)       │
├───────────────────────┤                          ├───────────────────────┤
│                       │                          │                       │
│  ┌─────────────────┐  │     API Requests         │  ┌─────────────────┐  │
│  │ React Frontend  │  │ ─────────────────────────▶  │ Spring Boot API │  │
│  │ (Static Files)  │  │                          │  │ (Java 17)       │  │
│  └─────────────────┘  │                          │  └────────┬────────┘  │
│                       │                          │           │           │
│  Features:            │                          │  Features:            │
│  • Global CDN         │                          │  • Auto-scaling       │
│  • HTTPS by default   │                          │  • Health checks      │
│  • Preview deploys    │                          │  • Zero-downtime      │
└───────────────────────┘                          └───────────┬───────────┘
                                                               │
                                                               │ JDBC
                                                               ▼
                                                   ┌───────────────────────┐
                                                   │        NEON           │
                                                   │   (Serverless PG)     │
                                                   ├───────────────────────┤
                                                   │                       │
                                                   │  ┌─────────────────┐  │
                                                   │  │   PostgreSQL    │  │
                                                   │  │   Database      │  │
                                                   │  └─────────────────┘  │
                                                   │                       │
                                                   │  Features:            │
                                                   │  • Auto-suspend       │
                                                   │  • Branching          │
                                                   │  • Point-in-time      │
                                                   └───────────────────────┘
```

### 2.2 Network Flow

| Step | From | To | Protocol | Port |
|------|------|-----|----------|------|
| 1 | Browser | Vercel CDN | HTTPS | 443 |
| 2 | Browser | Render API | HTTPS | 443 |
| 3 | Render | Neon DB | TCP/SSL | 5432 |

---

## 3. CI/CD Pipeline

### 3.1 Pipeline Overview

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    PUSH      │────▶│    BUILD     │────▶│    TEST      │────▶│   DEPLOY     │
│  to GitHub   │     │  Compile &   │     │  Run Tests   │     │  Production  │
│              │     │  Lint        │     │              │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
       │                    │                    │                    │
       │              ┌─────┴─────┐        ┌─────┴─────┐        ┌─────┴─────┐
       │              │           │        │           │        │           │
       ▼              ▼           ▼        ▼           ▼        ▼           ▼
  ┌─────────┐   ┌─────────┐ ┌─────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
  │ Trigger │   │ Backend │ │Frontend │ │ Unit   │ │ Integ  │ │ Render │ │ Vercel │
  │ on main │   │ Maven   │ │ npm     │ │ Tests  │ │ Tests  │ │ Deploy │ │ Deploy │
  └─────────┘   └─────────┘ └─────────┘ └────────┘ └────────┘ └────────┘ └────────┘
```

### 3.2 Backend Workflow

**File:** `.github/workflows/backend.yml`

```yaml
name: Backend CI/CD

on:
  push:
    branches: [main, develop]
    paths: ['backend/**']
  pull_request:
    branches: [main]
    paths: ['backend/**']

jobs:
  test:
    name: Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run tests
        working-directory: ./backend
        run: ./mvnw test
      
      - name: Build JAR
        working-directory: ./backend
        run: ./mvnw package -DskipTests
      
      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: backend-jar
          path: backend/target/*.jar

  deploy:
    name: Deploy to Render
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to Render
        run: |
          curl -X POST \
            "https://api.render.com/v1/services/$RENDER_SERVICE_ID/deploys" \
            -H "Authorization: Bearer $RENDER_API_KEY"
```

### 3.3 Frontend Workflow

**File:** `.github/workflows/frontend.yml`

```yaml
name: Frontend CI/CD

on:
  push:
    branches: [main, develop]
    paths: ['frontend/**']
  pull_request:
    branches: [main]
    paths: ['frontend/**']

jobs:
  test:
    name: Test & Build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci
      
      - name: Run tests
        working-directory: ./frontend
        run: npm test -- --run
      
      - name: Build
        working-directory: ./frontend
        run: npm run build

  deploy:
    name: Deploy to Vercel
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - run: npm install -g vercel@latest
      - run: vercel --prod --token=${{ secrets.VERCEL_TOKEN }}
```

---

## 4. Deployment Configuration

### 4.1 Docker Configuration

**File:** `backend/Dockerfile`

```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Key Features:**
- Multi-stage build (smaller final image)
- Alpine base (minimal footprint)
- JRE-only runtime (reduced attack surface)

### 4.2 Environment Configuration

**File:** `backend/.env.example`

```env
# Database (Neon PostgreSQL)
DATABASE_URL=jdbc:postgresql://your-neon-host/your-database
DATABASE_USER=your-username
DATABASE_PASSWORD=your-password

# JWT Security
JWT_SECRET=your-secure-random-string-at-least-64-chars

# CORS
CORS_ORIGINS=https://your-frontend.vercel.app

# Server
SERVER_PORT=8080
```

---

## 5. Environment Setup

### 5.1 Neon Database Setup

1. Create account at [neon.tech](https://neon.tech)
2. Create new project
3. Copy connection string

```
postgres://user:pass@ep-xxx.us-east-2.aws.neon.tech/dbname?sslmode=require
```

### 5.2 Render Backend Setup

1. Create account at [render.com](https://render.com)
2. New → Web Service
3. Connect GitHub repository
4. Configure:
   - **Root Directory:** `backend`
   - **Build Command:** `./mvnw package -DskipTests`
   - **Start Command:** `java -jar target/*.jar`
5. Add environment variables

### 5.3 Vercel Frontend Setup

1. Create account at [vercel.com](https://vercel.com)
2. Import Git repository
3. Configure:
   - **Root Directory:** `frontend`
   - **Framework Preset:** Vite
4. Add environment variable:
   - `VITE_API_URL=https://your-backend.onrender.com/api`

### 5.4 GitHub Secrets

| Secret | Value Source |
|--------|--------------|
| `RENDER_API_KEY` | Render Dashboard → Account Settings |
| `RENDER_SERVICE_ID` | Render Dashboard → Service Settings |
| `VERCEL_TOKEN` | Vercel → Settings → Tokens |
| `VERCEL_ORG_ID` | Vercel → Settings → General |
| `VERCEL_PROJECT_ID` | Vercel → Project Settings |
| `VITE_API_URL` | Your Render backend URL |

---

## 6. Deployment Procedures

### 6.1 Initial Deployment

```bash
# 1. Push code to GitHub
git add .
git commit -m "Initial deployment"
git push origin main

# 2. GitHub Actions triggers automatically

# 3. Monitor deployments at:
#    - https://github.com/your-repo/actions
#    - https://dashboard.render.com
#    - https://vercel.com/dashboard
```

### 6.2 Rollback Procedure

**Render:**
```bash
# Via Render Dashboard
1. Go to Events tab
2. Find previous successful deploy
3. Click "Redeploy"
```

**Vercel:**
```bash
# Via Vercel Dashboard
1. Go to Deployments tab
2. Find previous deployment
3. Click "..." → "Promote to Production"
```

### 6.3 Monitoring Endpoints

| Endpoint | Purpose | Expected Response |
|----------|---------|-------------------|
| `/api/health` | Backend health | `{"status": "UP"}` |
| `/_vite/ping` | Frontend health | 200 OK |

---

## 7. Deliverables

### 7.1 Files Created

| File | Purpose |
|------|---------|
| `.github/workflows/backend.yml` | Backend CI/CD pipeline |
| `.github/workflows/frontend.yml` | Frontend CI/CD pipeline |
| `backend/Dockerfile` | Docker containerization |
| `backend/.env.example` | Environment template |
| `docs/DEPLOYMENT.md` | Deployment guide |

### 7.2 Production Checklist

| Item | Status |
|------|--------|
| CI/CD Pipeline | ✅ Configured |
| Docker Support | ✅ Dockerfile created |
| Environment Config | ✅ Template provided |
| Health Checks | ✅ Endpoint available |
| SSL/HTTPS | ✅ Automatic via platforms |
| Deployment Guide | ✅ Documentation complete |

---

**End of Phase 5 Report**
