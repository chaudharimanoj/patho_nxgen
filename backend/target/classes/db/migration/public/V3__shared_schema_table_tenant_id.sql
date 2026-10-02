-- Migration V3: Table-level tenant_id isolation schema consolidation

ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS tenant_code VARCHAR(32);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS domain VARCHAR(255);
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(64) DEFAULT 'STARTER';
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS features JSONB DEFAULT '{}'::jsonb;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS configuration JSONB DEFAULT '{}'::jsonb;
ALTER TABLE public.tenants ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS idx_tenants_tenant_code ON public.tenants(tenant_code) WHERE tenant_code IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenants_domain ON public.tenants(domain) WHERE domain IS NOT NULL;

-- Operational business tables with table-level tenant_id isolation

CREATE TABLE IF NOT EXISTS public.patients (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mrn                     VARCHAR(32) NOT NULL,
    full_name               VARCHAR(120) NOT NULL,
    date_of_birth           DATE NOT NULL,
    gender                  VARCHAR(16) NOT NULL,
    phone                   VARCHAR(20),
    email                   VARCHAR(120),
    address                 VARCHAR(255),
    insurance_provider      VARCHAR(64),
    corporate_account_code  VARCHAR(64),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              VARCHAR(120),
    updated_by              VARCHAR(120)
);
ALTER TABLE public.patients ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_patient_tenant_phone ON public.patients(tenant_id, phone);

CREATE TABLE IF NOT EXISTS public.samples (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    barcode           VARCHAR(32) NOT NULL,
    patient_id        UUID NOT NULL REFERENCES public.patients(id) ON DELETE CASCADE,
    test_type         VARCHAR(64) NOT NULL,
    status            VARCHAR(24) NOT NULL DEFAULT 'REGISTERED',
    collected_by      VARCHAR(64),
    verified_by       VARCHAR(64),
    approved_by       VARCHAR(64),
    rejection_reason  TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        VARCHAR(120),
    updated_by        VARCHAR(120)
);
ALTER TABLE public.samples ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_sample_tenant_patient ON public.samples(tenant_id, patient_id);

CREATE TABLE IF NOT EXISTS public.invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number  VARCHAR(32) NOT NULL,
    patient_id      UUID NOT NULL REFERENCES public.patients(id) ON DELETE CASCADE,
    amount          NUMERIC(12,2) NOT NULL,
    payment_mode    VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);
ALTER TABLE public.invoices ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_invoice_tenant_patient ON public.invoices(tenant_id, patient_id);

CREATE TABLE IF NOT EXISTS public.inventory_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(120) NOT NULL,
    type                VARCHAR(20) NOT NULL,
    quantity_on_hand    INTEGER NOT NULL DEFAULT 0,
    reorder_threshold   INTEGER NOT NULL DEFAULT 0,
    expiry_date         DATE,
    vendor              VARCHAR(120),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          VARCHAR(120),
    updated_by          VARCHAR(120)
);
ALTER TABLE public.inventory_items ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_inventory_tenant_item ON public.inventory_items(tenant_id, name);

CREATE TABLE IF NOT EXISTS public.referrals (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referring_doctor_id  UUID NOT NULL,
    patient_id           UUID NOT NULL REFERENCES public.patients(id) ON DELETE CASCADE,
    source               VARCHAR(120),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           VARCHAR(120),
    updated_by           VARCHAR(120)
);
ALTER TABLE public.referrals ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_referrals_tenant_patient ON public.referrals(tenant_id, patient_id);

CREATE TABLE IF NOT EXISTS public.ledger_entries (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type         VARCHAR(16) NOT NULL,
    amount       NUMERIC(12,2) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120)
);
ALTER TABLE public.ledger_entries ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_ledger_tenant ON public.ledger_entries(tenant_id);

CREATE TABLE IF NOT EXISTS public.user_profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_user_id  VARCHAR(120) NOT NULL,
    employee_code     VARCHAR(32),
    display_name      VARCHAR(120) NOT NULL,
    email             VARCHAR(120),
    department        VARCHAR(64),
    designation       VARCHAR(64),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        VARCHAR(120),
    updated_by        VARCHAR(120)
);
ALTER TABLE public.user_profiles ADD COLUMN IF NOT EXISTS tenant_id UUID REFERENCES public.tenants(id) ON DELETE CASCADE;
ALTER TABLE public.user_profiles ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE public.user_profiles ADD COLUMN IF NOT EXISTS updated_by VARCHAR(120);
CREATE INDEX IF NOT EXISTS idx_user_profiles_tenant ON public.user_profiles(tenant_id);
