# Phase 6: Monitoring

## Distributed Micro-Banking System

---

**Document Version:** 1.0  
**Date:** December 22, 2024  
**Author:** Development Team  
**Project:** Micro-Banking System  

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Health Check Implementation](#2-health-check-implementation)
3. [Logging Configuration](#3-logging-configuration)
4. [Request Tracing](#4-request-tracing)
5. [Metrics & Monitoring](#5-metrics--monitoring)
6. [Monitoring Endpoints](#6-monitoring-endpoints)
7. [Deliverables](#7-deliverables)

---

## 1. Executive Summary

This document details the monitoring phase of the Distributed Micro-Banking System. The implementation includes health checks, structured logging, request tracing, and metrics collection to ensure observability in production.

### Monitoring Capabilities

| Capability | Implementation |
|------------|----------------|
| Health Checks | Spring Actuator + Custom DB indicator |
| Logging | Logback with file rotation |
| Request Tracing | MDC with request IDs |
| Metrics | Prometheus-compatible export |

---

## 2. Health Check Implementation

### 2.1 Spring Actuator Configuration

**application.properties:**
```properties
# Actuator Endpoints (Monitoring)
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
management.endpoint.health.probes.enabled=true
management.health.db.enabled=true
```

### 2.2 Custom Database Health Indicator

**DatabaseHealthIndicator.java:**
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Connected")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.down().build();
    }
}
```

### 2.3 Health Response Example

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "status": "Connected"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## 3. Logging Configuration

### 3.1 Logback Configuration

**logback-spring.xml:**
```xml
<configuration>
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS} [%thread] [%X{requestId:-SYSTEM}] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>

    <!-- File Appender with Rolling -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/microbanking.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/microbanking.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 3.2 Log Levels

| Logger | Level | Purpose |
|--------|-------|---------|
| `com.microbanking` | INFO | Application logs |
| `com.microbanking.service` | DEBUG | Service layer details |
| `org.hibernate.SQL` | WARN | SQL queries |
| `org.springframework.security` | WARN | Security events |

---

## 4. Request Tracing

### 4.1 Request Logging Filter

**RequestLoggingFilter.java:**
```java
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        // Add to MDC for log correlation
        MDC.put("requestId", requestId);
        
        logger.info("→ {} {} [{}]", 
            request.getMethod(), 
            request.getRequestURI(), 
            requestId);
        
        filterChain.doFilter(request, response);
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("← {} {} [{}ms]", 
            response.getStatus(), 
            request.getRequestURI(), 
            duration);
        
        MDC.clear();
    }
}
```

### 4.2 Sample Log Output

```
14:32:15.123 [http-nio-8080-exec-1] [abc12345] INFO  c.m.config.RequestLoggingFilter - → POST /api/auth/login [abc12345]
14:32:15.234 [http-nio-8080-exec-1] [abc12345] INFO  c.m.service.AuthService - User login attempt: user@example.com
14:32:15.345 [http-nio-8080-exec-1] [abc12345] INFO  c.m.config.RequestLoggingFilter - ← 200 /api/auth/login [222ms]
```

---

## 5. Metrics & Monitoring

### 5.1 Prometheus Configuration

**application.properties:**
```properties
management.metrics.export.prometheus.enabled=true
management.metrics.tags.application=${spring.application.name}
```

### 5.2 Available Metrics

| Metric | Description |
|--------|-------------|
| `http_server_requests` | Request count, latency |
| `jvm_memory_used` | JVM memory usage |
| `hikaricp_connections` | Database connections |
| `system_cpu_usage` | CPU utilization |

### 5.3 Prometheus Scrape Config

```yaml
scrape_configs:
  - job_name: 'microbanking'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

---

## 6. Monitoring Endpoints

### 6.1 Actuator Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator/health` | GET | Application health status |
| `/actuator/health/liveness` | GET | Kubernetes liveness probe |
| `/actuator/health/readiness` | GET | Kubernetes readiness probe |
| `/actuator/info` | GET | Application info |
| `/actuator/metrics` | GET | Available metrics |
| `/actuator/prometheus` | GET | Prometheus metrics export |

### 6.2 Custom Health Endpoint

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Simple health check |

---

## 7. Deliverables

### 7.1 Files Created

| File | Purpose |
|------|---------|
| `DatabaseHealthIndicator.java` | Custom database health check |
| `RequestLoggingFilter.java` | Request tracing with MDC |
| `logback-spring.xml` | Logging configuration |

### 7.2 Files Modified

| File | Changes |
|------|---------|
| `pom.xml` | Added spring-boot-starter-actuator |
| `application.properties` | Actuator & metrics config |

### 7.3 Monitoring Checklist

| Item | Status |
|------|--------|
| Health Endpoints | ✅ Configured |
| Database Health | ✅ Custom indicator |
| Request Logging | ✅ With request ID |
| File Logging | ✅ With rotation |
| Prometheus Metrics | ✅ Enabled |
| Kubernetes Probes | ✅ Available |

---

**End of Phase 6 Report**
