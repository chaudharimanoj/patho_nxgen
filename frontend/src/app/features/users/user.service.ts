import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface TenantUser {
  id: string;
  keycloakUserId: string;
  username: string;
  displayName: string;
  email: string;
  employeeCode?: string;
  department?: string;
  designation?: string;
  status: 'ACTIVE' | 'DISABLED' | 'SUSPENDED';
  roles: string[];
  createdAt: string;
  updatedAt?: string;
}

export interface CreateTenantUserPayload {
  username: string;
  password?: string;
  displayName: string;
  email: string;
  employeeCode?: string;
  department?: string;
  designation?: string;
  roles: string[];
}

export interface TenantRole {
  roleName: string;
  displayName: string;
  category: string;
  permissions: string[];
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tenant/users`;
  private readonly rolesUrl = `${environment.apiBaseUrl}/tenant/roles`;

  getUsers(query?: string, status?: string, department?: string): Observable<TenantUser[]> {
    let params = new HttpParams();
    if (query && query.trim()) params = params.set('query', query.trim());
    if (status && status.trim()) params = params.set('status', status.trim());
    if (department && department.trim()) params = params.set('department', department.trim());

    return this.http.get<ApiResponse<TenantUser[]>>(this.baseUrl, { params }).pipe(
      map(res => res.data || [])
    );
  }

  getUserById(id: string): Observable<TenantUser> {
    return this.http.get<ApiResponse<TenantUser>>(`${this.baseUrl}/${id}`).pipe(
      map(res => res.data)
    );
  }

  createUser(payload: CreateTenantUserPayload): Observable<TenantUser> {
    return this.http.post<ApiResponse<TenantUser>>(this.baseUrl, payload).pipe(
      map(res => res.data)
    );
  }

  updateUser(id: string, data: Partial<TenantUser>): Observable<TenantUser> {
    return this.http.put<ApiResponse<TenantUser>>(`${this.baseUrl}/${id}`, data).pipe(
      map(res => res.data)
    );
  }

  updateStatus(id: string, status: 'ACTIVE' | 'DISABLED' | 'SUSPENDED'): Observable<TenantUser> {
    return this.http.put<ApiResponse<TenantUser>>(`${this.baseUrl}/${id}/status`, { status }).pipe(
      map(res => res.data)
    );
  }

  resetPassword(id: string, newPassword?: string): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/${id}/reset-password`, { newPassword: newPassword || 'Password123!' }).pipe(
      map(res => res.message)
    );
  }

  assignRoles(id: string, roles: string[]): Observable<TenantUser> {
    return this.http.put<ApiResponse<TenantUser>>(`${this.baseUrl}/${id}/roles`, { roles }).pipe(
      map(res => res.data)
    );
  }

  getRoles(): Observable<TenantRole[]> {
    return this.http.get<ApiResponse<TenantRole[]>>(this.rolesUrl).pipe(
      map(res => res.data || [])
    );
  }
}
