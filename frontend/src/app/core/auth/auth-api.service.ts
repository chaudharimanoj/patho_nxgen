import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthStore } from './auth.store';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  tokenType: string;
  username: string;
  tenantSlug: string;
  tenantName: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly authStore = inject(AuthStore);
  private readonly baseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  login(credentials: { username: string; password: string }, tenantSlug?: string): Observable<AuthResponse> {
    let headers = new HttpHeaders();
    if (tenantSlug && tenantSlug.trim()) {
      headers = headers.set('X-Tenant-Slug', tenantSlug.trim());
    }

    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials, { headers }).pipe(
      tap((res) => {
        if (res && res.accessToken) {
          this.authStore.setAuth({
            token: res.accessToken,
            username: res.username,
            roles: res.roles,
            tenantSlug: res.tenantSlug,
          });
          localStorage.setItem('patho_access_token', res.accessToken);
          if (res.refreshToken) {
            localStorage.setItem('patho_refresh_token', res.refreshToken);
          }
          if (res.tenantSlug) {
            localStorage.setItem('patho_tenant_slug', res.tenantSlug);
          }
          if (res.username) {
            localStorage.setItem('patho_username', res.username);
          }
          if (res.roles) {
            localStorage.setItem('patho_roles', JSON.stringify(res.roles));
          }
        }
      })
    );
  }

  logout(): void {
    this.authStore.clearAuth();
    localStorage.removeItem('patho_access_token');
    localStorage.removeItem('patho_refresh_token');
    localStorage.removeItem('patho_tenant_slug');
    localStorage.removeItem('patho_username');
    localStorage.removeItem('patho_roles');
  }
}
