import { signalStore, withState, withMethods, patchState } from '@ngrx/signals';

export interface UiState {
  sidebarCollapsed: boolean;
  theme: 'light' | 'dark';
}

const initialState: UiState = {
  sidebarCollapsed: false,
  theme: 'light',
};

/**
 * NgRx Signal Store for UI state.
 * Replaces the Zustand useUiStore from the React app.
 */
export const UiStore = signalStore(
  { providedIn: 'root' },
  withState<UiState>(initialState),
  withMethods((store) => ({
    toggleSidebar(): void {
      patchState(store, { sidebarCollapsed: !store.sidebarCollapsed() });
    },
    setTheme(theme: 'light' | 'dark'): void {
      patchState(store, { theme });
    },
  }))
);
