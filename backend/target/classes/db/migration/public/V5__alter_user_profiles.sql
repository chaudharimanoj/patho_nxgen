-- Migration V5: Add missing audit columns to user_profiles

ALTER TABLE public.user_profiles ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE public.user_profiles ADD COLUMN IF NOT EXISTS updated_by VARCHAR(120);
