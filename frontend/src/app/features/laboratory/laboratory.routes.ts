import { Routes } from '@angular/router';

export const LABORATORY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/sample-list/sample-list.component').then((m) => m.SampleListComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/sample-register/sample-register.component').then(
        (m) => m.SampleRegisterComponent
      ),
  },
  {
    path: 'by-barcode',
    loadComponent: () =>
      import('./pages/sample-lookup/sample-lookup.component').then(
        (m) => m.SampleLookupComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/sample-detail/sample-detail.component').then(
        (m) => m.SampleDetailComponent
      ),
  },
  {
    path: ':id/transition/:status',
    loadComponent: () =>
      import('./pages/sample-transition/sample-transition.component').then(
        (m) => m.SampleTransitionComponent
      ),
  },
];
