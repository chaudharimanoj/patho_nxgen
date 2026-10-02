import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlanCatalog, SubscriptionUsage, UpdateSubscriptionRequest } from './subscription.model';

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {
  private readonly http = inject(HttpClient);

  getTenantSubscription(tenantId?: string): Observable<SubscriptionUsage> {
    const url = tenantId
      ? `/api/v1/platform/subscriptions/tenants/${tenantId}`
      : '/api/v1/tenant/subscription';
    return this.http
      .get<ApiResponse<SubscriptionUsage>>(url)
      .pipe(map((res) => res.data));
  }

  getCatalog(): Observable<PlanCatalog> {
    return this.http
      .get<ApiResponse<PlanCatalog>>('/api/v1/platform/subscriptions/catalog')
      .pipe(map((res) => res.data));
  }

  updateTenantSubscription(tenantId: string, request: UpdateSubscriptionRequest): Observable<SubscriptionUsage> {
    return this.http
      .put<ApiResponse<SubscriptionUsage>>(`/api/v1/platform/subscriptions/tenants/${tenantId}`, request)
      .pipe(map((res) => res.data));
  }
}
