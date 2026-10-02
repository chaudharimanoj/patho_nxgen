import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from '../../../core/http/api-client.service';
import { Patient, PatientFormValues, PageResponse } from '../models/patient.models';

/**
 * Patient API service — port of React patientApi.ts.
 * All HTTP is done through ApiClientService (JWT injected via interceptor).
 */
@Injectable({ providedIn: 'root' })
export class PatientApiService {
  private readonly api = inject(ApiClientService);

  search(query: string, page = 0, size = 20): Observable<PageResponse<Patient>> {
    return this.api.get<PageResponse<Patient>>('/patients', { query, page, size });
  }

  getById(id: string): Observable<Patient> {
    return this.api.get<Patient>(`/patients/${id}`);
  }

  register(payload: PatientFormValues): Observable<Patient> {
    return this.api.post<Patient>('/patients', payload);
  }

  update(id: string, payload: PatientFormValues): Observable<Patient> {
    return this.api.put<Patient>(`/patients/${id}`, payload);
  }
}
