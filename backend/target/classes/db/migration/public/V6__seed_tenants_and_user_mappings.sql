-- Migration V6: Seed Tenants, Subscriptions, and User Mappings for Multi-Tenant Keycloak setup

-- 1. Seed 3 Tenants with keycloak_realm matching 'pathonxgen-tenant-<slug>'
INSERT INTO public.tenants (id, slug, legal_name, keycloak_realm, status, tenant_code, domain, subscription_plan)
VALUES 
  ('a1b2c3d4-0001-4000-8000-000000000001', 'demo', 'Demo Pathology Labs', 'pathonxgen-tenant-demo', 'ACTIVE', 'DEMO-001', 'demo.pathonxgen.com', 'ENTERPRISE'),
  ('a1b2c3d4-0002-4000-8000-000000000002', 'acme', 'Acme Diagnostics', 'pathonxgen-tenant-acme', 'ACTIVE', 'ACME-002', 'acme.pathonxgen.com', 'PROFESSIONAL'),
  ('a1b2c3d4-0003-4000-8000-000000000003', 'metro', 'Metro Care Diagnostics', 'pathonxgen-tenant-metro', 'ACTIVE', 'METRO-003', 'metro.pathonxgen.com', 'STARTER')
ON CONFLICT (slug) DO UPDATE 
SET legal_name = EXCLUDED.legal_name,
    keycloak_realm = EXCLUDED.keycloak_realm,
    status = EXCLUDED.status;

-- 2. Seed Subscriptions for the 3 Tenants
INSERT INTO public.subscriptions (id, tenant_id, plan_id, status, billing_cycle)
SELECT 
  gen_random_uuid(),
  t.id,
  p.id,
  'ACTIVE',
  'MONTHLY'
FROM public.tenants t
CROSS JOIN public.plans p
WHERE p.code = 'STARTER' AND t.slug IN ('demo', 'acme', 'metro')
ON CONFLICT DO NOTHING;

-- 3. Seed 6 Tenant User Profiles (2 users per tenant)
-- Demo Tenant Users
INSERT INTO public.user_profiles (id, tenant_id, keycloak_user_id, employee_code, display_name, email, department, designation, status)
VALUES
  (gen_random_uuid(), 'a1b2c3d4-0001-4000-8000-000000000001', 'demo_admin', 'EMP-DEMO-01', 'Demo Admin User', 'admin@demo-lab.com', 'Administration', 'Tenant Administrator', 'ACTIVE'),
  (gen_random_uuid(), 'a1b2c3d4-0001-4000-8000-000000000001', 'demo_pathologist', 'EMP-DEMO-02', 'Dr. Demo Pathologist', 'pathologist@demo-lab.com', 'Pathology', 'Senior Pathologist', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Acme Tenant Users
INSERT INTO public.user_profiles (id, tenant_id, keycloak_user_id, employee_code, display_name, email, department, designation, status)
VALUES
  (gen_random_uuid(), 'a1b2c3d4-0002-4000-8000-000000000002', 'acme_admin', 'EMP-ACME-01', 'Acme Admin User', 'admin@acme-diag.com', 'Administration', 'Tenant Administrator', 'ACTIVE'),
  (gen_random_uuid(), 'a1b2c3d4-0002-4000-8000-000000000002', 'acme_tech', 'EMP-ACME-02', 'Acme Lab Technician', 'tech@acme-diag.com', 'Laboratory', 'Lab Technician', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Metro Tenant Users
INSERT INTO public.user_profiles (id, tenant_id, keycloak_user_id, employee_code, display_name, email, department, designation, status)
VALUES
  (gen_random_uuid(), 'a1b2c3d4-0003-4000-8000-000000000003', 'metro_admin', 'EMP-METRO-01', 'Metro Admin User', 'admin@metro-care.com', 'Administration', 'Tenant Administrator', 'ACTIVE'),
  (gen_random_uuid(), 'a1b2c3d4-0003-4000-8000-000000000003', 'metro_doctor', 'EMP-METRO-02', 'Dr. Metro Clinician', 'doctor@metro-care.com', 'Clinical', 'Consulting Doctor', 'ACTIVE')
ON CONFLICT DO NOTHING;
