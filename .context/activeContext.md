# Active Context

## Current Focus
- Completed Custom Direct Login UI and Backend Authentication for Patho NXGen Multi-Tenant Platform.

## Recent Accomplishments
1. **Implemented Backend Tenant Public Config & Direct Auth Endpoints**:
   - `GET /api/v1/tenant/public-config`: Exposes tenant branding, logo, legal name, and primary theme colors.
   - `POST /api/v1/auth/login`: Direct backend authentication via Keycloak Resource Owner Password Credentials Grant.
   - Authoritative tenant resolution via Host header / `X-Tenant-Slug` header without accepting `tenantId` in request payload.
   - Custom exceptions (`InvalidCredentialsException`, `AccountDisabledException`, `TenantInactiveException`, `AuthServiceUnavailableException`) and sanitized `GlobalExceptionHandler`.
   - Security permissions updated in `SecurityConfig.java`.

2. **Implemented Frontend Custom Direct Login UI**:
   - Created `TenantService` and `AuthApiService`.
   - Replaced Keycloak UI redirect with embedded direct login UI in `LoginComponent`.
   - Tenant sub-URL input with quick preset buttons (`demo`, `acme`, `metro`, `platform`).
   - Dynamic tenant header logo and legal name.
   - Show/hide password visibility toggle (eye icon).
   - "Remember me" checkbox.
   - Loading indicator state `[ ◌ Signing in... ]`.
   - Sanitized error alert banner for failed authentication attempts.

3. **Fixed Duplicated Endpoint Path**:
   - Identified and resolved duplicated `/api/v1` prefix in `AuthApiService` (`/api/v1/api/v1/auth/login` -> `/api/v1/auth/login`) and `TenantService` (`/api/v1/api/v1/tenant/public-config` -> `/api/v1/tenant/public-config`).

4. **Verification**:
   - Verified backend compilation (0 errors).
   - Verified `GET /api/v1/tenant/public-config` returns valid tenant public config payload.
   - Verified `POST /api/v1/auth/login` returns HTTP 401 for invalid credentials and full AuthResponse JWT tokens for valid credentials (`demo_admin` / `Password123!`).

Memory Bank successfully updated.
