import { inject } from '@angular/core';
import Keycloak from 'keycloak-js';
import { AuthStore } from './auth.store';
import { buildKeycloakConfig, resolveRealmFromCurrentLocation } from './keycloak.config';

let keycloakInstance: Keycloak | null = null;

export function getKeycloakInstance(): Keycloak | null {
  return keycloakInstance;
}

/**
 * APP_INITIALIZER factory for Keycloak.
 * Mirrors the React AuthProvider's keycloak.init() flow:
 *  1. Resolve realm dynamically from URL 'iss' param or hostname
 *  2. Init Keycloak with check-sso + PKCE
 *  3. On success → populate AuthStore
 *  4. On token expiry → refresh or clear + re-login
 *  5. On auth logout → clear AuthStore
 */
export function keycloakInitializer(): () => Promise<void> {
  const authStore = inject(AuthStore);

  return async () => {
    try {
      const target = resolveRealmFromCurrentLocation();
      console.log('[Auth] Initializing Keycloak for realm:', target.realm, 'tenant:', target.tenantSlug);

      const config = buildKeycloakConfig(target.tenantSlug, target.realm);
      const kc = new Keycloak({
        url: config.url,
        realm: config.realm,
        clientId: config.clientId,
      });
      keycloakInstance = kc;

      const authenticated = await kc.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      });

      if (authenticated && kc.token) {
        const parsed = kc.tokenParsed as Record<string, unknown> | undefined;
        const resolvedTenant =
          (parsed?.['tenant'] as string | undefined) ?? target.tenantSlug;

        authStore.setAuth({
          token: kc.token,
          username: parsed?.['preferred_username'] as string | undefined,
          roles: (parsed?.['realm_access'] as { roles?: string[] })?.roles ?? [],
          tenantSlug: resolvedTenant,
        });

        console.log('[Auth] Login successful', {
          username: parsed?.['preferred_username'],
          tenantSlug: resolvedTenant,
          realm: target.realm,
        });

        // Clean up URL if redirected back from Keycloak on /login route
        if (window.location.pathname === '/login') {
          window.history.replaceState({}, document.title, '/dashboard');
        }
      } else {
        console.log('[Auth] No authenticated user for realm:', target.realm);
        authStore.clearAuth();
      }

      // Token expiry handler — refresh silently or re-login
      kc.onTokenExpired = async () => {
        try {
          const refreshed = await kc.updateToken(30);
          if (refreshed && kc.token) {
            const parsed = kc.tokenParsed as Record<string, unknown> | undefined;
            authStore.setAuth({
              token: kc.token,
              username: parsed?.['preferred_username'] as string | undefined,
              roles: (parsed?.['realm_access'] as { roles?: string[] })?.roles ?? [],
              tenantSlug:
                (parsed?.['tenant'] as string | undefined) ?? target.tenantSlug,
            });
          }
        } catch {
          console.error('[Auth] Token refresh failed, re-login required');
          authStore.clearAuth();
          kc.login();
        }
      };

      kc.onAuthLogout = () => {
        authStore.clearAuth();
      };
    } catch (e) {
      console.error('[Auth] Keycloak initialization failed', e);
      authStore.clearAuth();
    }
  };
}
