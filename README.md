# Patho NXGen Platform

A modern, multi-tenant SaaS platform built for pathology laboratories, diagnostic centers, and enterprise healthcare networks. 

---

## 🏗️ System Architecture

### **Backend Stack**
- **Framework:** Java 25 + Spring Boot 3.5 (Modular Monolith)
- **Multi-Tenancy:** Schema-per-tenant isolation (`tenant_<slug>` schemas) managed via `TenantRoutingDataSource` & Flyway migrations.
- **Security & IAM:** Keycloak (OAuth2 / OIDC resource server integration, realm/tenant mapping).
- **Data Persistence & Cache:** PostgreSQL (Spring Data JPA), Redis for caching & session state.
- **Messaging & Event Streaming:** RabbitMQ (Domain events: sample status transitions, notifications).
- **Object Storage & Scheduling:** MinIO (Medical reports, attachment storage), Quartz Scheduler.
- **Observability & Docs:** Micrometer, Prometheus, OpenTelemetry Tracing, SpringDoc OpenAPI (Swagger UI).

### **Frontend Stack**
- **Framework & Monorepo:** Angular 22 + Nx Workspace
- **State Management:** `@ngrx/signals` (Signal Stores)
- **UI & Components:** Angular Material, AG Grid Angular, Tailwind CSS, ECharts (`ngx-echarts`)
- **Authentication:** Keycloak Angular (`keycloak-angular` & `keycloak-js`)

### **Infrastructure**
- **Reverse Proxy:** Nginx (`infra/nginx/nginx.conf`) routing `/api` -> Spring Boot backend and `/` -> Angular frontend.
- **Orchestration:** Docker Compose (`docker-compose.yml`) for local multi-container environment.

---

## 📂 Repository Structure

```
patho-nxgen/
├── backend/                        # Spring Boot 3.5 / Java 25 Modular Monolith
│   └── src/main/java/com/pathonxgen/
│       ├── config/                 # Security, JPA auditing, OpenAPI, Redis, RabbitMQ, MinIO
│       ├── core/                   # Schema-per-tenant routing, Keycloak security mapping, exceptions
│       ├── common/                 # BaseEntity, ApiResponse, PageResponse
│       └── modules/                # Domain Capabilities:
│           ├── auth/               # IAM & user authentication integration
│           ├── billing/            # Invoicing, payment processing, fee schedules
│           ├── crm/                # Client & doctor relationship management
│           ├── finance/            # Financial ledgers & transaction management
│           ├── integration/        # External lab device & LIMS interfaces
│           ├── inventory/          # Reagents, kits, stock management
│           ├── laboratory/         # Sample lifecycle machine & report approvals
│           ├── notification/       # Email/SMS/WhatsApp alert system
│           ├── patient/            # Patient registration, demographics, medical history
│           ├── reporting/          # Diagnostic report generation & analytics
│           ├── scheduler/          # Automated job runner
│           ├── subscription/       # Tenant subscription plans & quota usage tracking
│           └── tenantadmin/        # Control plane for tenant management
│
├── frontend/                       # Angular 22 / Nx Workspace Frontend
│   └── src/app/
│       ├── core/                   # Auth guards, Keycloak initialization, HTTP interceptors, Stores
│       ├── features/               # Feature Modules:
│       │   ├── auth/               # Login & authentication pages
│       │   ├── dashboard/          # Analytics & executive summary dashboard
│       │   ├── laboratory/         # Sample registration, tracking, transition & lookup
│       │   ├── organizations/      # Branch & organization management
│       │   ├── patient/            # Patient management & registration
│       │   ├── subscription/       # Subscription plans & quota monitoring
│       │   └── users/              # User management & RBAC controls
│       └── shared/                 # Layouts (MainLayout), navigation sidebar, reusable components
│
├── infra/nginx/                    # Nginx proxy configuration
└── docker-compose.yml               # Local infrastructure (Postgres, Redis, RabbitMQ, MinIO, Keycloak)
```

---

## ⚡ Core Domain Flows

1. **Sample Lifecycle State Machine (Backend)**:
   - Enforces state transitions: `Collection` ➔ `Barcode` ➔ `Dispatch` ➔ `Processing` ➔ `QC` ➔ `Verification` ➔ `Approval` ➔ `Report Delivery`.
   - Emits domain events to RabbitMQ at each phase for auditability and notifications.

2. **Schema-per-Tenant Multi-Tenancy**:
   - Each tenant has an isolated PostgreSQL schema (`tenant_<slug>`).
   - Dynamic schema switching based on tenant context extracted from Keycloak JWT claims.
   - Per-tenant Flyway migrations for schema evolution.

3. **Subscription & Quota Control**:
   - Real-time tracking of branch counts, monthly sample volume, and user seats against active subscription tiers.

---

## 🚀 Getting Started

### Prerequisites
- **Java JDK 25**
- **Node.js 20+ & npm / Nx CLI**
- **Docker & Docker Compose**

### 1. Launch Infrastructure Services
Start PostgreSQL, Redis, RabbitMQ, MinIO, and Keycloak:
```bash
docker compose up -d postgres redis rabbitmq minio keycloak
```

### 2. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
- API Base URL: `http://localhost:8080`
- Swagger OpenAPI Docs: `http://localhost:8080/swagger-ui.html`

### 3. Run the Frontend
```bash
cd frontend
npm install
npx nx serve
# or npm start
```
- Application URL: `http://localhost:4200`

---

## 🔒 Keycloak Configuration Note

Before end-to-end authentication works locally:
1. Access Keycloak Console at `http://localhost:8080/auth` (or configured port).
2. Ensure a tenant realm (e.g., `patho-<tenant>`) exists with client `patho-nxgen-frontend`.
3. Configure protocol mappers for `tenant` and `realm_access.roles` in the JWT token payload.

