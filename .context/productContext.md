# Product Context & System Architecture

## Product Context & UX Goals
- **Multi-Tenant Pathology SaaS**: Patho NXGen allows individual pathology labs (tenants) to operate in complete data isolation via individual PostgreSQL schemas (`tenant_<slug>`).
- **User Experience**: Premium, modern interface with dark mode options, responsive sidebar layouts, quick-access patient & laboratory management pages, AG Grid list view tables, and ECharts analytics dashboards.

## System Architecture

### Multi-Tenancy Strategy
- **Schema-per-tenant isolation**: Shared PostgreSQL database instance with Flyway migrations for `public` schema (control plane) and `tenant_<slug>` schema (per-tenant data).
- Dynamic tenant resolution via `TenantContext` & `TenantRoutingDataSource` in Spring Boot.
- Keycloak integration using a realm-per-tenant architecture (`patho-<tenant>`).

### Backend Architecture (`/backend`)
- **Modular Monolith Structure**:
  - `config/`: Security, JPA auditing, Redis/RabbitMQ/MinIO beans.
  - `core/`: Tenant routing, Keycloak security/RBAC, global exception handler.
  - `common/`: Shared base entities & standard REST API response DTOs.
  - `modules/`: Feature modules including `patient`, `laboratory`, `billing`, `inventory`, `crm`, `finance`, `reporting`, `notification`, `scheduler`, `integration`, and `tenantadmin`.

### Frontend Architecture (`/frontend`)
- **Angular 19 + Nx**:
  - `app/core/`: `AuthStore`, `UiStore`, Keycloak initializer, HTTP API interceptors.
  - `app/shared/`: Sidenav navigation, layouts, reusable UI primitives.
  - `app/features/`: Isolated feature modules (`auth`, `dashboard`, `patient`, `laboratory`).
