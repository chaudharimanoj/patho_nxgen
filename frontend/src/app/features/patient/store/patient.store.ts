import { signalStore, withState, withMethods, withComputed, patchState } from '@ngrx/signals';
import { inject, computed } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { PatientApiService } from '../services/patient-api.service';
import { Patient, PatientFormValues, PageResponse } from '../models/patient.models';

export interface PatientState {
  patients: PageResponse<Patient> | null;
  selectedPatient: Patient | null;
  loading: boolean;
  submitting: boolean;
  error: string | null;
}

const initialState: PatientState = {
  patients: null,
  selectedPatient: null,
  loading: false,
  submitting: false,
  error: null,
};

/**
 * NgRx Signal Store for Patient feature.
 * Replaces TanStack Query hooks: usePatientSearch, usePatient, useRegisterPatient.
 */
export const PatientStore = signalStore(
  { providedIn: 'root' },
  withState<PatientState>(initialState),
  withComputed((store) => ({
    totalPages: computed(() => store.patients()?.totalPages ?? 0),
    isLastPage: computed(() => store.patients()?.last ?? true),
    patientList: computed(() => store.patients()?.content ?? []),
  })),
  withMethods((store, api = inject(PatientApiService)) => ({
    async loadPatients(query: string, page: number): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const result = await firstValueFrom(api.search(query, page));
        patchState(store, { patients: result, loading: false });
      } catch (e: unknown) {
        patchState(store, { loading: false, error: (e as Error).message });
      }
    },

    async loadPatient(id: string): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const patient = await firstValueFrom(api.getById(id));
        patchState(store, { selectedPatient: patient, loading: false });
      } catch (e: unknown) {
        patchState(store, { loading: false, error: (e as Error).message });
      }
    },

    async registerPatient(payload: PatientFormValues): Promise<Patient> {
      patchState(store, { submitting: true, error: null });
      try {
        const patient = await firstValueFrom(api.register(payload));
        patchState(store, { submitting: false });
        return patient;
      } catch (e: unknown) {
        patchState(store, { submitting: false, error: (e as Error).message });
        throw e;
      }
    },

    clearSelected(): void {
      patchState(store, { selectedPatient: null });
    },
  }))
);
