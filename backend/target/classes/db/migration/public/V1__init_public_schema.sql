-- Public (control-plane) schema: platform-wide tables shared across all
-- tenants. Never put tenant business data here.

CREATE TABLE IF NOT EXISTS public.tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(64) NOT NULL UNIQUE,
    legal_name      VARCHAR(255) NOT NULL,
    keycloak_realm  VARCHAR(64) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PROVISIONING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(32) NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    monthly_price   NUMERIC(12,2) NOT NULL,
    annual_price    NUMERIC(12,2),
    max_users       INTEGER,
    features        JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS public.subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES public.tenants(id),
    plan_id         UUID NOT NULL REFERENCES public.plans(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    billing_cycle   VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    renews_at       TIMESTAMPTZ,
    cancelled_at    TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS public.platform_billing_invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES public.tenants(id),
    subscription_id UUID NOT NULL REFERENCES public.subscriptions(id),
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    paid_at         TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS public.audit_log (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       UUID REFERENCES public.tenants(id),
    actor           VARCHAR(120),
    action          VARCHAR(120) NOT NULL,
    entity_type     VARCHAR(120),
    entity_id       VARCHAR(64),
    metadata        JSONB,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant ON public.subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant ON public.audit_log(tenant_id);
