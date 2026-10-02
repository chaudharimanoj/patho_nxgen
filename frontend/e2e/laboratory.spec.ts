import { test, expect } from '@playwright/test';

test.describe('Laboratory Module', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/samples');
  });

  test('should display sample list page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Samples', { timeout: 10000 });
    await expect(page.locator('#btn-register-sample')).toBeVisible();
    await expect(page.locator('#btn-barcode-lookup')).toBeVisible();
  });

  test('should navigate to sample register', async ({ page }) => {
    await page.click('#btn-register-sample');
    await expect(page).toHaveURL('/samples/register');
    await expect(page.locator('h1')).toContainText('Register New Sample');
    await expect(page.locator('#input-barcode')).toBeVisible();
    await expect(page.locator('#input-patient-id')).toBeVisible();
    await expect(page.locator('#input-test-type')).toBeVisible();
  });

  test('register sample form validates required fields', async ({ page }) => {
    await page.goto('/samples/register');
    await page.click('#btn-submit-sample');
    await expect(page.locator('mat-error').first()).toBeVisible();
  });

  test('should navigate to barcode lookup page', async ({ page }) => {
    await page.click('#btn-barcode-lookup');
    await expect(page).toHaveURL('/samples/by-barcode');
    await expect(page.locator('h1')).toContainText('Lookup Sample by Barcode');
    await expect(page.locator('#input-lookup-barcode')).toBeVisible();
  });

  test('barcode lookup shows not-found for unknown barcode', async ({ page }) => {
    await page.goto('/samples/by-barcode');
    await page.fill('#input-lookup-barcode', 'UNKNOWN-BC-99999');
    await page.click('#btn-lookup');
    await expect(page.locator('text=Sample Not Found')).toBeVisible({ timeout: 8000 });
  });
});
