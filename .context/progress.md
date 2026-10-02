# Progress Checklist

## Core Infrastructure & Setup
- [x] Docker Compose setup for PostgreSQL, Redis, RabbitMQ, MinIO, and Keycloak
- [x] Backend JDK 24 environment configuration and Spring Boot compilation (`mvnw spring-boot:run`)
- [x] Frontend Node 22 ESM cross-platform fix and Angular Nx serve initialization (`npm start`)
- [x] Full stack active local execution (Infrastructure + Backend + Frontend)
- [x] Memory Bank (`.context/`) creation and synchronization
- [x] User Credentials reference file creation ([`USER_CREDENTIALS.md`](file:///d:/Projects/patho-nxgen-platform/patho-nxgen/USER_CREDENTIALS.md))
- [x] Background infrastructure log stream task launched (`docker compose logs -f`)

## Identity & Multi-Tenancy Provisioning
- [x] Provision Platform Realms (`pathonxgen-platform` & `patho-platform`) with platform roles & 3 platform users
- [x] Provision 3 Tenant Realms matching `pathonxgen-tenant-<slug>` (`pathonxgen-tenant-demo`, `pathonxgen-tenant-acme`, `pathonxgen-tenant-metro`)
- [x] Register `patho-nxgen-frontend` OIDC client per realm with custom claim mappers (`tenant`, `realm_access.roles`)
- [x] Seed PostgreSQL `public.tenants`, `public.subscriptions`, and `public.user_profiles` mapping database records to Keycloak realms & user IDs
- [x] Fix Keycloak realm name alignment (`pathonxgen-tenant-<slug>`) enabling `200 OK` on authentication endpoints
- [x] Resolve login page stall & automate Keycloak authentication redirect in `LoginComponent`
- [x] Fix Keycloak logout TypeError in `MainLayoutComponent`
- [x] Resolve OAuth2 authorization code race condition (`400 Bad Request / invalid_code`)
- [x] Add client/tenant sub-URL textbox, live realm preview, and preset chips to `LoginComponent`
- [x] Fix `onLoginSubmit` TypeError by constructing direct OIDC authorization redirect URL
- [x] Add dynamic URL `iss` parameter realm extraction enabling seamless multi-realm token exchange for Platform & Tenant realms
- [x] Implement backend `GET /api/v1/tenant/public-config` and `POST /api/v1/auth/login` direct auth endpoints
- [x] Implement backend authoritative tenant resolution, Keycloak password grant service, and sanitized error exception handling
- [x] Implement production-ready frontend Custom Direct Login UI with dynamic branding, password eye toggle, remember me, loading states, and error alerts

## Backend Modules
- [x] Multi-tenancy routing infrastructure (`TenantContext`, `TenantRoutingDataSource`)
- [x] Patient Module (reference pattern: Entity, DTOs, Service, Controller, Flyway migrations)
- [x] Laboratory Module / Sample State Machine (Collection -> Verification -> Approval)
- [ ] Tenant Provisioning Saga (Keycloak realm auto-creation & dynamic tenant schema migration)
- [ ] Billing & Invoice sequence generation via PostgreSQL sequences
- [ ] Stubbed Modules expansion (Inventory, CRM, Finance, Reporting, Notifications, Scheduler)

## Frontend Features
- [x] Angular 19 Nx workspace layout & Angular Material Sidenav integration
- [x] Keycloak Authentication integration & AuthStore (NgRx Signal Store)
- [x] Patient Management (AG Grid list view, Patient Register form)
- [x] Laboratory Management (Sample list, registration, detail view, status transitions)
- [ ] Dashboard analytics integration with live backend API endpoints
