import { environment } from '../../../environments/environment';

/**
 * Resolves the tenant slug from the current hostname.
 * - On localhost/127.0.0.1 → returns devTenantSlug from environment
 * - On <tenant>.example.com → returns first subdomain segment
 */
export function resolveTenantSlugFromHost(hostname: string): string {
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return environment.devTenantSlug;
  }
  const parts = hostname.split('.');
  return parts.length > 2 ? parts[0] : environment.devTenantSlug;
}

/**
 * Resolves the realm and tenant slug dynamically from URL parameters or hostname.
 * - Inspects Keycloak's 'iss' query parameter when returning from authentication.
 * - Fallbacks to hostname resolution.
 */
export function resolveRealmFromCurrentLocation(): { realm: string; tenantSlug: string } {
  if (typeof window !== 'undefined' && window.location?.search) {
    const urlParams = new URLSearchParams(window.location.search);
    const iss = urlParams.get('iss');
    if (iss && iss.includes('/realms/')) {
      const realmName = decodeURIComponent(iss.substring(iss.lastIndexOf('/realms/') + 8));
      let tenantSlug = realmName;
      if (realmName.startsWith('pathonxgen-tenant-')) {
        tenantSlug = realmName.replace('pathonxgen-tenant-', '');
      } else if (realmName === 'pathonxgen-platform' || realmName === 'patho-platform') {
        tenantSlug = 'platform';
      }
      return { realm: realmName, tenantSlug };
    }
  }

  const tenantSlug = resolveTenantSlugFromHost(window.location.hostname);
  const realm = (tenantSlug === 'platform' || tenantSlug === 'pathonxgen-platform')
    ? 'pathonxgen-platform'
    : `pathonxgen-tenant-${tenantSlug}`;
  return { realm, tenantSlug };
}

/**
 * Builds Keycloak configuration for the resolved tenant/realm.
 */
export function buildKeycloakConfig(tenantSlug: string, customRealm?: string) {
  const realm = customRealm || (
    tenantSlug === 'platform' || tenantSlug === 'pathonxgen-platform' 
      ? 'pathonxgen-platform' 
      : `pathonxgen-tenant-${tenantSlug}`
  );
  return {
    url: environment.keycloakUrl,
    realm: realm,
    clientId: 'patho-nxgen-frontend',
  };
}
