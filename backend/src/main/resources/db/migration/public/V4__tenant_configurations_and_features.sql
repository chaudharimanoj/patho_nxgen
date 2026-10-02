-- Migration V4: Tenant Configuration and Feature Flags

CREATE TABLE IF NOT EXISTS public.tenant_configurations (
    tenant_id          UUID PRIMARY KEY REFERENCES public.tenants(id) ON DELETE CASCADE,
    timezone           VARCHAR(64) DEFAULT 'UTC',
    language           VARCHAR(16) DEFAULT 'en',
    date_format        VARCHAR(32) DEFAULT 'YYYY-MM-DD',
    logo_url           VARCHAR(255),
    theme              VARCHAR(32) DEFAULT 'LIGHT',
    report_header      TEXT,
    report_footer      TEXT,
    enabled_modules    JSONB NOT NULL DEFAULT '{"patient": true, "laboratory": true, "billing": true, "analytics": true, "sms": false, "whatsapp": false}'::jsonb,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tenant_config_modules ON public.tenant_configurations USING gin (enabled_modules);
