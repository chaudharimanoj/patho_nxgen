import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TenantPublicConfig {
  tenantId: string;
  tenantName: string;
  legalName?: string;
  slug: string;
  status?: string;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  customCss?: string;
  features?: Record<string, boolean>;
}

@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  readonly currentTenantConfig = signal<TenantPublicConfig | null>(null);
  readonly isLoadingConfig = signal<boolean>(false);

  fetchPublicConfig(tenantSlug?: string): Observable<TenantPublicConfig | null> {
    this.isLoadingConfig.set(true);

    let headers = new HttpHeaders();
    if (tenantSlug && tenantSlug.trim()) {
      headers = headers.set('X-Tenant-Slug', tenantSlug.trim());
    }

    return this.http.get<TenantPublicConfig>(`${this.baseUrl}/tenant/public-config`, { headers }).pipe(
      tap((config) => {
        this.currentTenantConfig.set(config);
        this.isLoadingConfig.set(false);
      }),
      catchError((err) => {
        console.warn('[TenantService] Failed to load tenant public config:', err);
        this.isLoadingConfig.set(false);
        this.currentTenantConfig.set(null);
        return of(null);
      })
    );
  }
}
