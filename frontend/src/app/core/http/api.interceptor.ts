import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStore } from '../auth/auth.store';

/**
 * Functional HTTP interceptor that attaches the Bearer token
 * to outgoing API requests and automatically logs out device sessions if
 * the backend returns HTTP 423 (Tenant Suspended/Deactivated) or 401 (Expired/Revoked Token).
 */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const router = inject(Router);
  const token = authStore.token() || localStorage.getItem('patho_access_token');

  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // 423 = Tenant Suspended/Deactivated (ignore on public config and login calls to prevent infinite loops)
      const isSuspended = error.status === 423 && !req.url.includes('/auth/login') && !req.url.includes('/tenant/public-config');
      
      // 401 = Token Expired/Revoked on an authenticated request (ignore 401 on login attempts)
      const isAuthTokenExpired = error.status === 401 && !!token && !req.url.includes('/auth/login');

      if (isSuspended || isAuthTokenExpired) {
        console.warn(`[ApiInterceptor] Caught HTTP ${error.status} (${error.error?.error || 'Account Locked/Suspended'}). Invalidating session across devices...`);
        
        authStore.clearAuth();
        localStorage.removeItem('patho_access_token');
        localStorage.removeItem('patho_refresh_token');
        localStorage.removeItem('patho_tenant_slug');
        localStorage.removeItem('patho_username');
        localStorage.removeItem('patho_roles');

        router.navigate(['/auth/login'], {
          queryParams: {
            reason: isSuspended ? 'suspended' : 'expired',
            msg: error.error?.error || (isSuspended 
              ? 'Your account has been suspended. You have been logged out on all devices.' 
              : 'Your session has expired. Please sign in again.')
          }
        });
      }
      return throwError(() => error);
    })
  );
};
