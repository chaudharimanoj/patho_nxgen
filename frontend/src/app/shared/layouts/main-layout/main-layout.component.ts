import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthStore } from '../../../core/auth/auth.store';
import { AuthApiService } from '../../../core/auth/auth-api.service';
import { UiStore } from '../../../core/ui/ui.store';
import { getKeycloakInstance } from '../../../core/auth/auth.initializer';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  platformOnly?: boolean;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent {
  protected readonly authStore = inject(AuthStore);
  protected readonly uiStore = inject(UiStore);

  readonly navItems: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/organizations', label: 'Organizations / Clients', icon: 'corporate_fare', platformOnly: true },
    { path: '/subscriptions', label: 'Subscription & Plans', icon: 'card_membership', platformOnly: true },
    { path: '/users', label: 'User & Role Access', icon: 'admin_panel_settings' },
    { path: '/patients', label: 'Patients', icon: 'people' },
    { path: '/samples', label: 'Laboratory', icon: 'science' },
  ];

  readonly visibleNavItems = computed(() => {
    const slug = this.authStore.tenantSlug();
    const roles = this.authStore.roles();
    const isPlatformAdmin = slug === 'platform' || roles.includes('SUPER_ADMIN') || roles.includes('PLATFORM_ADMIN');
    return this.navItems.filter(item => !item.platformOnly || isPlatformAdmin);
  });

  private readonly authApiService = inject(AuthApiService);
  private readonly router = inject(Router);

  logout(): void {
    const keycloak = getKeycloakInstance();
    if (keycloak && keycloak.authenticated) {
      try {
        keycloak.logout({ redirectUri: window.location.origin + '/login' });
      } catch (e) {
        console.warn('[Logout] Keycloak JS logout failed, falling back to local logout:', e);
        this.authApiService.logout();
        this.router.navigate(['/login']);
      }
    } else {
      this.authApiService.logout();
      this.router.navigate(['/login']);
    }
  }
}
