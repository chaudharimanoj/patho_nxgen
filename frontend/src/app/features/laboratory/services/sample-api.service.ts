import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from '../../../core/http/api-client.service';
import {
  Sample,
  SampleFormValues,
  SampleTransitionValues,
  PageResponse,
} from '../models/laboratory.models';

/**
 * Sample API service — port of React sampleApi.ts.
 */
@Injectable({ providedIn: 'root' })
export class SampleApiService {
  private readonly api = inject(ApiClientService);

  search(query: string, page = 0, size = 20): Observable<PageResponse<Sample>> {
    return this.api.get<PageResponse<Sample>>('/samples', { query, page, size });
  }

  getById(id: string): Observable<Sample> {
    return this.api.get<Sample>(`/samples/${id}`);
  }

  register(payload: SampleFormValues): Observable<Sample> {
    return this.api.post<Sample>('/samples', payload);
  }

  transition(
    sampleId: string,
    status: string,
    payload: SampleTransitionValues
  ): Observable<Sample> {
    return this.api.put<Sample>(`/samples/${sampleId}/transition/${status}`, payload);
  }

  getByBarcode(barcode: string): Observable<Sample> {
    return this.api.get<Sample>(`/samples/barcode/${barcode}`);
  }

  getByPatientId(patientId: string): Observable<Sample[]> {
    return this.api.get<Sample[]>(`/samples/patient/${patientId}`);
  }

  getByStatus(status: string): Observable<Sample[]> {
    return this.api.get<Sample[]>(`/samples/status/${status}`);
  }
}
