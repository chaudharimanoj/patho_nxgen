-- Applied once per tenant schema (tenant_<slug>) during provisioning, via a
-- dedicated Flyway.configure().schemas("tenant_<slug>")
-- .locations("classpath:db/migration/tenant") instance - see
-- TenantProvisioningService TODOs. Not picked up by the main spring.flyway
-- config, which only targets 'public'.

CREATE TABLE IF NOT EXISTS patients (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mrn                     VARCHAR(32) NOT NULL UNIQUE,
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
CREATE INDEX IF NOT EXISTS idx_patient_phone ON patients(phone);

CREATE TABLE IF NOT EXISTS samples (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    barcode           VARCHAR(32) NOT NULL UNIQUE,
    patient_id        UUID NOT NULL REFERENCES patients(id),
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
CREATE INDEX IF NOT EXISTS idx_sample_patient ON samples(patient_id);

CREATE TABLE IF NOT EXISTS invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number  VARCHAR(32) NOT NULL UNIQUE,
    patient_id      UUID NOT NULL REFERENCES patients(id),
    amount          NUMERIC(12,2) NOT NULL,
    payment_mode    VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS inventory_items (
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

CREATE TABLE IF NOT EXISTS referrals (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    referring_doctor_id  UUID NOT NULL,
    patient_id           UUID NOT NULL REFERENCES patients(id),
    source               VARCHAR(120),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by           VARCHAR(120),
    updated_by           VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type         VARCHAR(16) NOT NULL,
    amount       NUMERIC(12,2) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120)
);

-- Per-tenant sequences backing MRN / invoice number generation; replaces
-- the in-memory AtomicLong placeholders in PatientService / InvoiceService.
CREATE SEQUENCE IF NOT EXISTS mrn_seq START 1;
CREATE SEQUENCE IF NOT EXISTS invoice_seq START 1;
