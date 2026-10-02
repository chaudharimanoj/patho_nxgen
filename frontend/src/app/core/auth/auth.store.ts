import { signalStore, withState, withMethods, withComputed, patchState } from '@ngrx/signals';
import { computed } from '@angular/core';

export interface AuthState {
  token: string | null;
  username: string | null;
  roles: string[];
  tenantSlug: string | null;
}

export function isTokenExpired(token: string | null): boolean {
  if (!token) return true;
  try {
    const parts = token.split('.');
    if (parts.length < 2) return true;
    const payload = JSON.parse(atob(parts[1]));
    if (!payload.exp) return false;
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp < currentTime;
  } catch {
    return true;
  }
}

function getStoredRoles(): string[] {
  try {
    const raw = localStorage.getItem('patho_roles');
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

const emptyState: AuthState = {
  token: null,
  username: null,
  roles: [],
  tenantSlug: null,
};

function getInitialState(): AuthState {
  const token = localStorage.getItem('patho_access_token');
  if (token && !isTokenExpired(token)) {
    return {
      token,
      username: localStorage.getItem('patho_username') || null,
      roles: getStoredRoles(),
      tenantSlug: localStorage.getItem('patho_tenant_slug') || null,
    };
  }
  // Token is expired or null: clear localStorage
  localStorage.removeItem('patho_access_token');
  localStorage.removeItem('patho_refresh_token');
  localStorage.removeItem('patho_tenant_slug');
  localStorage.removeItem('patho_username');
  localStorage.removeItem('patho_roles');
  return emptyState;
}

/**
 * NgRx Signal Store for authentication state.
 *
 * Usage:
 *   private authStore = inject(AuthStore);
 *   this.authStore.token()           // current token signal
 *   this.authStore.isAuthenticated() // computed boolean signal
 *   this.authStore.setAuth({...})    // update auth state
 *   this.authStore.clearAuth()       // clear auth state
 */
export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState<AuthState>(getInitialState()),
  withComputed((store) => ({
    isAuthenticated: computed(() => !!store.token() && !isTokenExpired(store.token())),
  })),
  withMethods((store) => ({
    setAuth(payload: {
      token: string;
      username?: string;
      roles?: string[];
      tenantSlug?: string;
    }): void {
      patchState(store, {
        token: payload.token,
        username: payload.username ?? null,
        roles: payload.roles ?? [],
        tenantSlug: payload.tenantSlug ?? null,
      });
    },

    clearAuth(): void {
      localStorage.removeItem('patho_access_token');
      localStorage.removeItem('patho_refresh_token');
      localStorage.removeItem('patho_tenant_slug');
      localStorage.removeItem('patho_username');
      localStorage.removeItem('patho_roles');
      patchState(store, emptyState);
    },

    hasRole(role: string): boolean {
      return store.roles().includes(role);
    },

    hasPermission(permission: string): boolean {
      const userRoles = store.roles();
      const roleMap: Record<string, string[]> = {
        SUPER_ADMIN: ['patients:read', 'patients:create', 'patients:update', 'samples:read', 'samples:create', 'samples:update', 'reports:read', 'reports:approve', 'users:manage', 'roles:manage', 'tenant:configure'],
        PLATFORM_ADMIN: ['patients:read', 'patients:create', 'patients:update', 'samples:read', 'samples:create', 'samples:update', 'reports:read', 'reports:approve', 'users:manage', 'roles:manage', 'tenant:configure'],
        TENANT_ADMIN: ['patients:read', 'patients:create', 'patients:update', 'samples:read', 'samples:create', 'samples:update', 'reports:read', 'reports:approve', 'users:manage', 'roles:manage', 'tenant:configure'],
        MANAGER: ['patients:read', 'patients:create', 'patients:update', 'samples:read', 'samples:create', 'samples:update', 'reports:read', 'reports:approve'],
        PATHOLOGIST: ['patients:read', 'samples:read', 'reports:read', 'reports:approve'],
        DOCTOR: ['patients:read', 'samples:read', 'reports:read'],
        LAB_TECHNICIAN: ['patients:read', 'samples:read', 'samples:create', 'samples:update', 'reports:read'],
        RECEPTIONIST: ['patients:read', 'patients:create', 'patients:update'],
      };
      return userRoles.some(r => {
        const perms = roleMap[r.toUpperCase().replace('ROLE_', '')] || [];
        return perms.includes(permission);
      });
    },
  }))
);
