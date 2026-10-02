INSERT INTO public.plans (code, name, monthly_price, annual_price, max_users, features)
VALUES
    ('STARTER', 'Starter', 4999.00, 49999.00, 5, '{"sms": false, "whatsapp": false, "advanced_analytics": false}'),
    ('PROFESSIONAL', 'Professional', 14999.00, 149999.00, 25, '{"sms": true, "whatsapp": true, "advanced_analytics": false}'),
    ('ENTERPRISE', 'Enterprise', 39999.00, 399999.00, NULL, '{"sms": true, "whatsapp": true, "advanced_analytics": true}')
ON CONFLICT (code) DO NOTHING;
