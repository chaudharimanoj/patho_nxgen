import { Routes } from '@angular/router';

export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/patient-list/patient-list.component').then(
        (m) => m.PatientListComponent
      ),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/patient-register/patient-register.component').then(
        (m) => m.PatientRegisterComponent
      ),
  },
];
