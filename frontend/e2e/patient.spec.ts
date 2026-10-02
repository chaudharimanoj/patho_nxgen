import { test, expect } from '@playwright/test';

// NOTE: These tests require a running backend + Keycloak with demo realm
// and a pre-authenticated session (or mock Keycloak).
// Run with: npx playwright test --project=chromium

test.describe('Patient Module', () => {
  test.beforeEach(async ({ page }) => {
    // Assumes session already authenticated (use storageState or mock KC)
    await page.goto('/patients');
  });

  test('should display patient list page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Patients', { timeout: 10000 });
    await expect(page.locator('#btn-register-patient')).toBeVisible();
    await expect(page.locator('#input-patient-search')).toBeVisible();
  });

  test('should navigate to register patient page', async ({ page }) => {
    await page.click('#btn-register-patient');
    await expect(page).toHaveURL('/patients/new');
    await expect(page.locator('h1')).toContainText('Register Patient');
  });

  test('register patient form should validate required fields', async ({ page }) => {
    await page.goto('/patients/new');
    await page.click('#btn-submit-patient');
    await expect(page.locator('mat-error')).toBeVisible();
  });
});
