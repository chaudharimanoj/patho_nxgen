import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should redirect to Keycloak login', async ({ page }) => {
    await page.goto('/');
    // Should redirect to Keycloak IDP (external URL with /realms/patho-*)
    await expect(page).toHaveURL(/keycloak|realms\/patho-|\/login/, { timeout: 10000 });
  });

  test('login page shows branded loading state', async ({ page }) => {
    // Login page briefly shows while Keycloak redirect fires
    await page.goto('/login');
    await expect(page.locator('text=Patho NXGen')).toBeVisible({ timeout: 5000 });
  });
});
