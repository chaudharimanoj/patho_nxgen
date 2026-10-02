import { signalStore, withState, withMethods, withComputed, patchState } from '@ngrx/signals';
import { inject, computed } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { SampleApiService } from '../services/sample-api.service';
import { AuthStore } from '../../../core/auth/auth.store';
import {
  Sample,
  SampleFormValues,
  SampleTransitionValues,
  PageResponse,
  Transition,
  ALLOWED_TRANSITIONS,
} from '../models/laboratory.models';

export interface SampleState {
  samples: PageResponse<Sample> | null;
  selectedSample: Sample | null;
  barcodeResult: Sample | null;
  loading: boolean;
  submitting: boolean;
  error: string | null;
}

const initialState: SampleState = {
  samples: null,
  selectedSample: null,
  barcodeResult: null,
  loading: false,
  submitting: false,
  error: null,
};

/**
 * NgRx Signal Store for Laboratory feature.
 * Replaces TanStack Query hooks: useSampleSearch, useSample, useRegisterSample,
 * useTransitionSample, useSampleByBarcode.
 */
export const SampleStore = signalStore(
  { providedIn: 'root' },
  withState<SampleState>(initialState),
  withComputed((store, authStore = inject(AuthStore)) => ({
    sampleList: computed(() => store.samples()?.content ?? []),
    totalPages: computed(() => store.samples()?.totalPages ?? 0),
    isLastPage: computed(() => store.samples()?.last ?? true),
    /** Available transitions for the selected sample filtered by user's roles */
    availableTransitions: computed((): Transition[] => {
      const sample = store.selectedSample();
      if (!sample) return [];
      const roles = authStore.roles();
      return (ALLOWED_TRANSITIONS[sample.status] ?? []).filter((t) =>
        roles.includes(t.requiredRole)
      );
    }),
  })),
  withMethods((store, api = inject(SampleApiService)) => ({
    async searchSamples(query: string, page: number): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const result = await firstValueFrom(api.search(query, page));
        patchState(store, { samples: result, loading: false });
      } catch (e: unknown) {
        patchState(store, { loading: false, error: (e as Error).message });
      }
    },

    async loadSample(id: string): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const sample = await firstValueFrom(api.getById(id));
        patchState(store, { selectedSample: sample, loading: false });
      } catch (e: unknown) {
        patchState(store, { loading: false, error: (e as Error).message });
      }
    },

    async registerSample(payload: SampleFormValues): Promise<Sample> {
      patchState(store, { submitting: true, error: null });
      try {
        const sample = await firstValueFrom(api.register(payload));
        patchState(store, { submitting: false });
        return sample;
      } catch (e: unknown) {
        patchState(store, { submitting: false, error: (e as Error).message });
        throw e;
      }
    },

    async transitionSample(
      sampleId: string,
      status: string,
      payload: SampleTransitionValues
    ): Promise<Sample> {
      patchState(store, { submitting: true, error: null });
      try {
        const sample = await firstValueFrom(api.transition(sampleId, status, payload));
        patchState(store, { submitting: false, selectedSample: sample });
        return sample;
      } catch (e: unknown) {
        patchState(store, { submitting: false, error: (e as Error).message });
        throw e;
      }
    },

    async lookupByBarcode(barcode: string): Promise<void> {
      patchState(store, { loading: true, barcodeResult: null, error: null });
      try {
        const sample = await firstValueFrom(api.getByBarcode(barcode));
        patchState(store, { barcodeResult: sample, loading: false });
      } catch (e: unknown) {
        patchState(store, { loading: false, barcodeResult: null, error: (e as Error).message });
      }
    },

    clearBarcodeResult(): void {
      patchState(store, { barcodeResult: null, error: null });
    },

    clearSelected(): void {
      patchState(store, { selectedSample: null });
    },
  }))
);
