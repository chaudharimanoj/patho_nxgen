import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ClientOrganization {
  id: string;
  slug: string;
  tenantCode: string;
  domain: string;
  legalName: string;
  keycloakRealm: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED' | 'PROVISIONING' | 'PENDING';
  subscriptionPlan: string;
  volumeTierCode?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ProvisionClientRequest {
  slug: string;
  tenantCode?: string;
  domain?: string;
  legalName: string;
  subscriptionPlan?: string;
  volumeTierCode?: string;
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/platform/tenants`;

  getOrganizations(): Observable<ClientOrganization[]> {
    return this.http.get<ApiResponse<ClientOrganization[]>>(this.baseUrl).pipe(
      map(res => res.data || [])
    );
  }

  updateStatus(id: string, status: 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'): Observable<ClientOrganization> {
    return this.http.put<ApiResponse<ClientOrganization>>(`${this.baseUrl}/${id}/status`, { status }).pipe(
      map(res => res.data)
    );
  }

  provisionOrganization(req: ProvisionClientRequest): Observable<ClientOrganization> {
    return this.http.post<ApiResponse<ClientOrganization>>(this.baseUrl, req).pipe(
      map(res => res.data)
    );
  }

  updateOrganization(id: string, data: Partial<ClientOrganization>): Observable<ClientOrganization> {
    return this.http.put<ApiResponse<ClientOrganization>>(`${this.baseUrl}/${id}`, data).pipe(
      map(res => res.data)
    );
  }

  deleteOrganization(id: string): Observable<void> {
    return this.http.delete<ApiResponse<string>>(`${this.baseUrl}/${id}`).pipe(
      map(() => void 0)
    );
  }
}
