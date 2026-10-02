import { Component, inject, OnInit, signal, effect } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthStore } from '../../../../core/auth/auth.store';
import { AuthApiService } from '../../../../core/auth/auth-api.service';
import { TenantService, TenantPublicConfig } from '../../../../core/tenant/tenant.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatProgressSpinnerModule],
  template: `
    <div class="min-h-screen w-full flex flex-col items-center justify-center bg-slate-950 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(120,119,198,0.3),rgba(255,255,255,0))] p-4 font-sans text-slate-100 relative overflow-hidden">
      
      <!-- Ambient Background Glows -->
      <div class="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-600/20 blur-[120px] rounded-full pointer-events-none"></div>
      <div class="absolute bottom-10 right-10 w-72 h-72 bg-blue-600/10 blur-[100px] rounded-full pointer-events-none"></div>

      <!-- Main Login Container -->
      <div class="w-full max-w-md z-10">
        
        <!-- Header Branding (Dynamic by Tenant) -->
        <div class="text-center mb-6">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-indigo-600/20 border border-indigo-500/30 text-indigo-400 mb-3 shadow-lg shadow-indigo-500/10 backdrop-blur-md overflow-hidden p-2">
            @if (tenantConfig()?.logoUrl) {
              <img [src]="tenantConfig()?.logoUrl" [alt]="tenantConfig()?.tenantName" class="w-full h-full object-contain" />
            } @else {
              <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
              </svg>
            }
          </div>

          <h1 class="text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
            {{ tenantConfig()?.tenantName || 'Patho NXGen Platform' }}
          </h1>
          <p class="mt-1 text-xs text-indigo-300 font-medium tracking-wide uppercase">
            {{ tenantConfig() ? 'Laboratory Sign In' : 'Pathology Diagnostic Platform' }}
          </p>
        </div>

        <!-- Glassmorphism Login Card -->
        <div class="rounded-3xl bg-slate-900/80 border border-slate-800/80 p-8 shadow-2xl backdrop-blur-xl transition-all">
          
          @if (authStore.isAuthenticated()) {
            <div class="flex flex-col items-center py-8 gap-4 text-center">
              <mat-spinner diameter="44" color="accent" />
              <p class="text-slate-300 text-sm font-medium">Authentication successful! Opening laboratory portal...</p>
            </div>
          } @else {

            <!-- Error Banner -->
            @if (errorMessage()) {
              <div class="mb-5 p-4 rounded-2xl bg-rose-950/70 border border-rose-800/80 flex items-start gap-3 text-rose-200 text-xs shadow-md animate-fade-in">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-rose-400 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div class="flex-1">
                  <span class="font-semibold text-rose-300">Authentication Error</span>
                  <p class="mt-0.5 opacity-90 leading-relaxed">{{ errorMessage() }}</p>
                </div>
              </div>
            }

            <form (submit)="onLoginSubmit($event)" class="space-y-5">
              
              <!-- Lab Name Field -->
              <div class="space-y-1.5">
                <label for="tenantInput" class="block text-xs font-semibold uppercase tracking-wider text-indigo-400">
                  Lab Name
                </label>
                
                <div class="relative flex items-center">
                  <input 
                    id="tenantInput"
                    name="tenantSlug"
                    type="text" 
                    [(ngModel)]="tenantSlug" 
                    (change)="onTenantSlugChange()"
                    placeholder="e.g. demo"
                    required
                    class="w-full px-4 py-2.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all shadow-inner"
                  />
                </div>
              </div>

              <!-- Username Field -->
              <div class="space-y-1.5">
                <label for="usernameInput" class="block text-xs font-medium text-slate-300">
                  Username or Email
                </label>
                <div class="relative flex items-center">
                  <span class="absolute left-3.5 text-slate-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </span>
                  <input 
                    id="usernameInput"
                    name="username"
                    type="text"
                    [(ngModel)]="username"
                    required
                    placeholder="Enter your username"
                    [disabled]="isSubmitting()"
                    class="w-full pl-10 pr-4 py-2.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all shadow-inner"
                  />
                </div>
              </div>

              <!-- Password Field with Show/Hide Toggle -->
              <div class="space-y-1.5">
                <div class="flex items-center justify-between">
                  <label for="passwordInput" class="block text-xs font-medium text-slate-300">
                    Password
                  </label>
                </div>
                <div class="relative flex items-center">
                  <span class="absolute left-3.5 text-slate-500">
                    <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </span>
                  <input 
                    id="passwordInput"
                    name="password"
                    [type]="showPassword() ? 'text' : 'password'"
                    [(ngModel)]="password"
                    required
                    placeholder="Enter your password"
                    [disabled]="isSubmitting()"
                    class="w-full pl-10 pr-11 py-2.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white text-sm placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all shadow-inner"
                  />
                  <button 
                    type="button"
                    (click)="togglePasswordVisibility()"
                    class="absolute right-3 text-slate-500 hover:text-slate-300 focus:outline-none p-1 rounded-md transition-colors"
                    [attr.aria-label]="showPassword() ? 'Hide password' : 'Show password'">
                    @if (showPassword()) {
                      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24M1 1l22 22" />
                      </svg>
                    } @else {
                      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    }
                  </button>
                </div>
                <p class="text-[11px] text-slate-500 font-mono pt-0.5">
                  Dev Password: <span class="text-indigo-300 font-semibold">Password123!</span>
                </p>
              </div>

              <!-- Remember Me & Actions -->
              <div class="flex items-center justify-between text-xs pt-1">
                <label class="flex items-center gap-2 text-slate-400 cursor-pointer select-none">
                  <input 
                    type="checkbox" 
                    [(ngModel)]="rememberMe" 
                    name="rememberMe"
                    class="rounded border-slate-700 bg-slate-950 text-indigo-600 focus:ring-indigo-500 focus:ring-offset-slate-900"
                  />
                  <span>Remember me</span>
                </label>
              </div>

              <!-- Submit Button with Loading State -->
              <button 
                type="submit"
                [disabled]="isSubmitting()"
                class="w-full py-3.5 px-4 bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold rounded-xl shadow-lg shadow-indigo-600/25 hover:shadow-indigo-500/35 active:scale-[0.99] transition-all flex items-center justify-center gap-2.5 cursor-pointer mt-2">
                @if (isSubmitting()) {
                  <mat-spinner diameter="20" color="primary" />
                  <span>Signing in...</span>
                } @else {
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                  </svg>
                  <span>Sign In</span>
                }
              </button>

            </form>

          }

        </div>

        <!-- Footer Info -->
        <footer class="mt-6 text-center text-xs text-slate-500">
          Patho NXGen Laboratory Platform &bull; Secure Authentication
        </footer>

      </div>

    </div>
  `,
  styles: [`:host { display: block; }`],
})
export class LoginComponent implements OnInit {
  protected readonly authStore = inject(AuthStore);
  private readonly authApiService = inject(AuthApiService);
  private readonly tenantService = inject(TenantService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  tenantSlug = 'demo';
  username = 'demo_admin';
  password = 'Password123!';
  rememberMe = false;

  readonly showPassword = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);
  readonly tenantConfig = this.tenantService.currentTenantConfig;

  readonly presets = [
    { slug: 'demo', name: 'Demo Pathology' },
    { slug: 'acme', name: 'Acme Diagnostics' },
    { slug: 'metro', name: 'Metro Care' },
    { slug: 'platform', name: 'Platform Admin' },
  ];

  constructor() {
    effect(() => {
      const reason = this.route.snapshot.queryParams['reason'];
      if (!reason && this.authStore.isAuthenticated()) {
        console.log('[Auth] Authenticated user detected, navigating to /dashboard');
        this.router.navigate(['/dashboard']);
      }
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params: Params) => {
      if (params['reason'] || params['msg']) {
        this.authStore.clearAuth();
        this.errorMessage.set(params['msg'] || (params['reason'] === 'expired'
          ? 'Your session has expired. Please sign in again.'
          : 'Laboratory account is SUSPENDED. Active sessions on all devices have been automatically logged out.'));
      }
    });

    if (!this.route.snapshot.queryParams['reason'] && this.authStore.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.loadTenantConfig();
  }

  selectPreset(slug: string): void {
    this.tenantSlug = slug;
    if (slug === 'demo') {
      this.username = 'demo_admin';
      this.password = 'Password123!';
    } else if (slug === 'acme') {
      this.username = 'acme_admin';
      this.password = 'Password123!';
    } else if (slug === 'metro') {
      this.username = 'metro_admin';
      this.password = 'Password123!';
    } else if (slug === 'platform') {
      this.username = 'superadmin';
      this.password = 'Password123!';
    }
    this.loadTenantConfig();
  }

  onTenantSlugChange(): void {
    this.loadTenantConfig();
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  private loadTenantConfig(): void {
    this.tenantService.fetchPublicConfig(this.tenantSlug).subscribe({
      next: (config) => {
        if (config && (config.status === 'SUSPENDED' || config.status === 'DEACTIVATED')) {
          this.errorMessage.set(`Laboratory account '${config.legalName || this.tenantSlug}' is ${config.status}. Active sessions on all devices have been logged out.`);
        }
      },
      error: (err) => {
        if (err.status === 423) {
          this.errorMessage.set(`Laboratory account '${this.tenantSlug}' is SUSPENDED. Please contact platform support.`);
        }
      }
    });
  }

  onLoginSubmit(event: Event): void {
    event.preventDefault();
    if (this.isSubmitting()) return;

    this.errorMessage.set(null);

    if (!this.username.trim() || !this.password) {
      this.errorMessage.set('Please enter both username and password.');
      return;
    }

    this.isSubmitting.set(true);

    this.authApiService.login({ username: this.username, password: this.password }, this.tenantSlug).subscribe({
      next: (res) => {
        console.log('[Auth] Direct login successful:', res);
        this.isSubmitting.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('[Auth] Direct login error:', err);
        this.isSubmitting.set(false);

        let msg = 'Authentication failed. Please verify your credentials.';
        if (err.status === 423) {
          msg = 'Laboratory account is suspended or locked. Please contact support.';
        } else if (err.error && err.error.message) {
          msg = err.error.message.replace(/Tenant/g, 'Laboratory');
        } else if (err.status === 401) {
          msg = 'Invalid username or password.';
        } else if (err.status === 403) {
          msg = 'Account is disabled. Please contact system administrator.';
        } else if (err.status === 503 || err.status === 0) {
          msg = 'Authentication service is temporarily unavailable. Please try again later.';
        }
        this.errorMessage.set(msg);
      },
    });
  }
}
