import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { authGuard } from './core/auth/auth.guard';

/**
 * Root route configuration.
 * Mirrors the React AppRouter + ProtectedRoute structure.
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'patients',
        loadChildren: () =>
          import('./features/patient/patient.routes').then((m) => m.PATIENT_ROUTES),
      },
      {
        path: 'samples',
        loadChildren: () =>
          import('./features/laboratory/laboratory.routes').then((m) => m.LABORATORY_ROUTES),
      },
      {
        path: 'organizations',
        loadComponent: () =>
          import('./features/organizations/pages/organization-list/organization-list.component').then(
            (m) => m.OrganizationListComponent
          ),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/users/pages/user-management/user-management.component').then(
            (m) => m.UserManagementComponent
          ),
      },
      {
        path: 'subscriptions',
        loadComponent: () =>
          import('./features/subscription/subscription-dashboard.component').then(
            (m) => m.SubscriptionDashboardComponent
          ),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
