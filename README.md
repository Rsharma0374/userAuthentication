# Identity Service

A production-ready Java Spring Boot service for account registration, authentication, session management, user profiles, and private object uploads.

## Architecture Overview

This service implements the architecture specified in `architecture.md`:

- **API Gateway**: Kong handles TLS ingress, routing, rate limiting, and request/response limits
- **Database**: PostgreSQL with Flyway migrations
- **Storage**: Amazon S3 with quarantine/clean bucket pattern for secure file uploads
- **Authentication**: Short-lived JWT access tokens (10 min) + rotating refresh tokens (30 days absolute, 7 days idle)
- **Password Hashing**: Argon2id with configurable work factors
- **MFA**: TOTP with encrypted secrets and recovery codes
- **Async Processing**: Transactional outbox pattern with leased workers
- **Observability**: Structured logging, OpenTelemetry, Prometheus metrics

## Features

### Account Management
- Email registration with verification
- Password reset workflow
- Email change workflow
- Account deletion

### Authentication
- Username/password login with Argon2id
- MFA (TOTP) with recovery codes
- JWT access tokens (asymmetric RS256)
- Refresh token rotation with replay detection
- Session management (list, revoke, revoke all)

### User Profiles
- Display name, locale, timezone
- Avatar management (linked to S3 objects)

### Object Storage
- Presigned POST uploads to quarantine bucket
- Malware scanning and image re-encoding
- Clean bucket for validated objects
- Authorized downloads with signed URLs
- Asynchronous deletion

### Security
- Rate limiting on auth endpoints
- CSRF protection for cookie operations
- Generic error responses (no account enumeration)
- Audit logging for security events
- Secure headers and CORS configuration

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Local Development

1. Start infrastructure:
```bash
docker-compose up -d
```

2. Build and run the application:
```bash
./mvnw spring-boot:run -Pdev
```

The API will be available at `http://localhost:8080/api`

### API Documentation

Swagger UI: `http://localhost:8080/api/swagger-ui.html`
OpenAPI Spec: `http://localhost:8080/api/api-docs`

### Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /v1/auth/register | Register new account |
| POST | /v1/auth/email/verify | Verify email |
| POST | /v1/auth/email/resend | Resend verification |
| POST | /v1/auth/login | Password login |
| POST | /v1/auth/mfa/verify | Complete MFA challenge |
| POST | /v1/auth/refresh | Rotate refresh token |
| POST | /v1/auth/logout | Revoke current session |
| POST | /v1/auth/forgot-password | Initiate password reset |
| POST | /v1/auth/reset-password | Complete password reset |
| POST | /v1/auth/change-password | Change password |
| GET | /v1/auth/sessions | List sessions |
| DELETE | /v1/auth/sessions/{id} | Revoke session |
| POST | /v1/auth/logout-all | Revoke all sessions |
| GET | /.well-known/jwks.json | Public JWKS |
| GET | /v1/users/me | Get profile |
| PATCH | /v1/users/me | Update profile |
| POST | /v1/users/me/avatar | Set avatar |
| POST | /v1/uploads | Create upload intent |
| POST | /v1/uploads/{id}/complete | Complete upload |
| GET | /v1/objects/{id}/download | Download object |
| DELETE | /v1/objects/{id} | Delete object |

## Configuration

### Profiles
- `dev` - Development (default)
- `prod` - Production

### Key Properties

```yaml
app:
  auth:
    jwt:
      issuer: "https://identity.example.com"
      audience: "identity-service"
      access-token-ttl: 10m
    refresh:
      absolute-ttl: 30d
      idle-ttl: 7d
    password:
      argon2-memory-kib: 65536
      argon2-iterations: 3
      argon2-parallelism: 4
  storage:
    s3:
      region: "us-east-1"
      quarantine-bucket: "identity-quarantine"
      clean-bucket: "identity-clean"
  notification:
    email:
      from-address: "noreply@example.com"
      base-url: "https://app.example.com"
```

## Testing

```bash
# Run all tests
./mvnw test -Ptest

# Run integration tests only
./mvnw verify -Ptest
```

## Building

```bash
# Build JAR
./mvnw clean package -Pprod

# Build Docker image
docker build -t identity-service:latest .
```

## Deployment

### Kubernetes
The service is designed to run as a stateless deployment with:
- Multiple replicas across availability zones
- HPA based on CPU/memory
- Pod disruption budgets
- Readiness/liveness probes

### Required Infrastructure
- PostgreSQL (managed, HA)
- Redis (for rate limiting)
- S3-compatible storage
- KMS for key management
- Kong API Gateway
- SMTP service

## Security Considerations

1. **Never expose actuator endpoints publicly** - Kong configuration restricts to internal IPs
2. **Use managed KMS/HSM** for JWT signing keys in production
3. **Enable TLS everywhere** - Kong terminates TLS
4. **Rotate secrets regularly** - Database passwords, JWT keys, encryption keys
5. **Monitor audit logs** - Alert on suspicious patterns

## License

Proprietary - All rights reserved.