# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Patho NXGen Platform is a multi-tenant SaaS platform for pathology laboratories. It consists of a Spring Boot 3.5/Java 25 modular monolith backend and an Angular 19/TypeScript/Nx frontend. The platform follows a schema-per-tenant multi-tenancy approach and uses Keycloak for authentication (realm-per-tenant).

## Prerequisites

- Java JDK 21
- Node.js >= 20
- Docker and Docker Compose
- (Optional) IDE with Java and TypeScript support (IntelliJ, VS Code, etc.)

## Backend Development

### Running the backend
```bash
cd backend
./mvnw spring-boot:run
```
The backend runs on `http://localhost:8080` by default (configurable via `SERVER_PORT` environment variable).

### Building the backend
```bash
cd backend
./mvnw clean package
```
Skipping tests:
```bash
./mvnw clean package -DskipTests
```

### Running tests
```bash
cd backend
./mvnw test
```

### Code style
The backend uses default Spotless formatting via the Maven wrapper (if configured). Run `./mvnw spotless:apply` to format code.

## Frontend Development (Angular + Nx)

### Install dependencies
```bash
cd frontend
npm install --legacy-peer-deps
```

### Development server
```bash
cd frontend
npm start   # runs Nx Angular dev server on http://localhost:4200
```
The frontend proxies `/api` requests to the backend at `http://localhost:8080` via `proxy.conf.json`.

### Building for production
```bash
cd frontend
npm run build   # outputs to ./dist/patho-nxgen/browser
```

### Running tests
```bash
cd frontend
npm test               # runs unit tests
npx playwright test    # runs Playwright E2E tests
```

## Docker Compose

The repository includes a `docker-compose.yml` that provides the required infrastructure:

- PostgreSQL
- Redis
- RabbitMQ
- MinIO (S3-compatible storage)
- Keycloak (OIDC provider)

### Start infrastructure services
```bash
docker compose up -d postgres redis rabbitmq minio keycloak
```

### Start the full stack (backend + frontend + infrastructure)
```bash
# Build images first (if needed)
docker compose build
# Then start all services
docker compose up
```

### Keycloak setup
Before end-to-end login works, you need to:
1. Create a realm named `patho-<tenant>` (replace `<tenant>` with your tenant slug, e.g., `patho-demo` or `patho-acme`).
2. Create a public client named `patho-nxgen-frontend` with:
   - Valid redirect URIs: `http://localhost:4200/*`
   - Web origins: `+`
   - Mapper for `token` realm access roles and a custom `tenant` claim (set to the realm name).

Refer to the README for details on configuring the JWT claim mapper.

## Code Structure

### Backend (`backend/src/main/java/com/pathonxgen/`)
- `config/` – Spring configuration
- `core/` – tenant context, security, global exception handling
- `common/` – shared DTOs & base entities
- `modules/` – domain modules (`patient`, `laboratory`, `billing`, etc.)

### Frontend (`frontend/src/`)
- `environments/` – API & Keycloak host configuration
- `app/`
  - `core/`
    - `auth/` – Keycloak slug resolution, `AuthStore` (NgRx Signal Store), `keycloakInitializer`, `authGuard`
    - `http/` – `apiInterceptor`, `ApiClientService` (HttpClient wrapper)
    - `ui/` – `UiStore` (NgRx Signal Store for sidebar & theme)
  - `shared/`
    - `layouts/` – `MainLayoutComponent` (Angular Material Sidenav + toolbar + user menu)
  - `features/`
    - `auth/` – login redirection screen
    - `dashboard/` – KPI cards & ECharts analytics charts
    - `patient/` – models, service, `PatientStore`, list (AG Grid Angular) & register pages
    - `laboratory/` – models, service, `SampleStore`, list, register, detail, transition, and lookup pages
- `e2e/` – Playwright spec files for authentication, patient, and laboratory modules

## Related Files

- `README.md` – high-level overview and setup instructions.
- `docker-compose.yml` – infrastructure services.
- `backend/Dockerfile` and `frontend/Dockerfile` – container images.