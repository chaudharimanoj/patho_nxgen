import { ApplicationConfig, APP_INITIALIZER, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { routes } from './app.routes';
import { apiInterceptor } from './core/http/api.interceptor';
import { keycloakInitializer } from './core/auth/auth.initializer';

/**
 * Root application configuration.
 * Replaces React main.tsx providers: AuthProvider, QueryClientProvider, BrowserRouter.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Angular Router — replaces React Router + BrowserRouter
    provideRouter(routes, withComponentInputBinding()),

    // HTTP client with JWT interceptor — replaces Axios + interceptor
    provideHttpClient(withInterceptors([apiInterceptor])),

    // Angular Material animations
    provideAnimationsAsync(),

    // APP_INITIALIZER — populates AuthStore after Keycloak init
    {
      provide: APP_INITIALIZER,
      useFactory: keycloakInitializer,
      multi: true,
    },
  ],
};
