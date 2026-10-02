-- Migration V7: Subscription Module (Tier & Volume Matrix, Usage Tracking, Branches)

CREATE TABLE IF NOT EXISTS public.subscription_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(32) NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    branch_scope    VARCHAR(32) NOT NULL DEFAULT 'SINGLE_LAB',
    features        JSONB NOT NULL DEFAULT '{}'::jsonb,
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.volume_tiers (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                   VARCHAR(32) NOT NULL UNIQUE,
    name                   VARCHAR(120) NOT NULL,
    monthly_patient_limit INTEGER, -- NULL or -1 indicates Unlimited
    description            VARCHAR(255),
    display_order          INTEGER NOT NULL DEFAULT 0,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.tenant_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL UNIQUE REFERENCES public.tenants(id) ON DELETE CASCADE,
    plan_id         UUID NOT NULL REFERENCES public.subscription_plans(id),
    volume_tier_id  UUID NOT NULL REFERENCES public.volume_tiers(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    billing_cycle   VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    renews_at       TIMESTAMPTZ NOT NULL DEFAULT (now() + INTERVAL '30 days'),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.tenant_monthly_usage (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,
    year_month      VARCHAR(7) NOT NULL,
    patient_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_monthly_usage UNIQUE (tenant_id, year_month)
);

CREATE TABLE IF NOT EXISTS public.branches (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,
    branch_code     VARCHAR(32) NOT NULL,
    name            VARCHAR(120) NOT NULL,
    city            VARCHAR(120),
    is_main_branch  BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_branch_code UNIQUE (tenant_id, branch_code)
);

CREATE INDEX IF NOT EXISTS idx_tenant_subscriptions_tenant ON public.tenant_subscriptions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_monthly_usage_tenant ON public.tenant_monthly_usage(tenant_id, year_month);
CREATE INDEX IF NOT EXISTS idx_branches_tenant ON public.branches(tenant_id);

-- Seed Subscription Tiers
INSERT INTO public.subscription_plans (code, name, branch_scope, display_order, features)
VALUES
    (
        'STARTER',
        'Starter Plan',
        'SINGLE_LAB',
        1,
        '{"billing": true, "reports": true, "inventory": true, "patient_portal": true, "advanced_analytics": false, "whatsapp_sms_integration": false, "doctor_commission_tracking": false, "custom_branding": false, "multi_branch_inventory": false, "inter_branch_transfer": false, "cross_branch_rbac": false}'::jsonb
    ),
    (
        'PREMIUM',
        'Premium Plan',
        'SINGLE_LAB',
        2,
        '{"billing": true, "reports": true, "inventory": true, "patient_portal": true, "advanced_analytics": true, "whatsapp_sms_integration": true, "doctor_commission_tracking": true, "custom_branding": true, "multi_branch_inventory": false, "inter_branch_transfer": false, "cross_branch_rbac": false}'::jsonb
    ),
    (
        'ENTERPRISE',
        'Enterprise Plan',
        'MULTI_BRANCH',
        3,
        '{"billing": true, "reports": true, "inventory": true, "patient_portal": true, "advanced_analytics": true, "whatsapp_sms_integration": true, "doctor_commission_tracking": true, "custom_branding": true, "multi_branch_inventory": true, "inter_branch_transfer": true, "cross_branch_rbac": true}'::jsonb
    )
ON CONFLICT (code) DO NOTHING;

-- Seed Volume Tiers
INSERT INTO public.volume_tiers (code, name, monthly_patient_limit, description, display_order)
VALUES
    ('VILLAGE', 'Village Tier', 100, 'Up to 100 patients per month', 1),
    ('TOWN', 'Town Tier', 500, 'Up to 500 patients per month', 2),
    ('CITY', 'City Tier', 5000, 'Up to 5,000 patients per month', 3),
    ('METRO', 'Metro City Tier', -1, 'Unlimited patients per month', 4)
ON CONFLICT (code) DO NOTHING;

-- Seed default tenant_subscriptions for all existing tenants
INSERT INTO public.tenant_subscriptions (tenant_id, plan_id, volume_tier_id, status)
SELECT 
    t.id,
    p.id,
    v.id,
    'ACTIVE'
FROM public.tenants t
CROSS JOIN public.subscription_plans p
CROSS JOIN public.volume_tiers v
WHERE p.code = 'PREMIUM' AND v.code = 'CITY'
ON CONFLICT (tenant_id) DO NOTHING;
