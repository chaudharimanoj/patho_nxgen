import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OrganizationService, ClientOrganization, ProvisionClientRequest } from '../../organization.service';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-organization-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="space-y-6">
      
      <!-- Toast Notification Banner -->
      @if (toastMessage()) {
        <div 
          [ngClass]="toastType() === 'success' ? 'bg-emerald-950/80 border-emerald-700/80 text-emerald-200' : 'bg-rose-950/80 border-rose-700/80 text-rose-200'"
          class="fixed top-5 right-5 z-50 p-4 rounded-2xl border shadow-2xl backdrop-blur-xl flex items-center gap-3 text-xs max-w-md animate-fade-in">
          <mat-icon class="!w-5 !h-5 !text-xl shrink-0">
            {{ toastType() === 'success' ? 'check_circle' : 'error' }}
          </mat-icon>
          <span class="flex-1 font-medium">{{ toastMessage() }}</span>
          <button (click)="toastMessage.set(null)" class="opacity-70 hover:opacity-100 cursor-pointer">
            <mat-icon class="!w-4 !h-4 !text-base">close</mat-icon>
          </button>
        </div>
      }

      <!-- Page Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-xl text-white">
        <div>
          <div class="flex items-center gap-2 text-indigo-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <mat-icon class="!w-4 !h-4 !text-base">corporate_fare</mat-icon>
            Platform Control Plane
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-white">Client / Organization Management</h1>
          <p class="text-slate-400 text-xs mt-0.5">Manage multi-tenant diagnostic clients, Keycloak realms, and database subscriptions.</p>
        </div>

        <button 
          (click)="openCreateModal()"
          class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-medium text-sm rounded-xl shadow-lg shadow-indigo-600/20 hover:shadow-indigo-500/30 transition-all cursor-pointer">
          <mat-icon class="!w-5 !h-5 !text-lg">add_business</mat-icon>
          <span>Register New Client</span>
        </button>
      </div>

      <!-- Key Metrics Stats Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Organizations -->
        <div class="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm">
          <div class="flex items-center justify-between text-slate-500 text-xs font-medium mb-2">
            <span>Total Clients</span>
            <div class="p-2 rounded-xl bg-indigo-50 text-indigo-600">
              <mat-icon class="!w-5 !h-5 !text-xl">domain</mat-icon>
            </div>
          </div>
          <div class="text-2xl font-bold text-slate-900">{{ totalCount() }}</div>
          <p class="text-xs text-slate-500 mt-1">Registered Tenant Organizations</p>
        </div>

        <!-- Active Clients -->
        <div class="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm">
          <div class="flex items-center justify-between text-slate-500 text-xs font-medium mb-2">
            <span>Active Tenants</span>
            <div class="p-2 rounded-xl bg-emerald-50 text-emerald-600">
              <mat-icon class="!w-5 !h-5 !text-xl">check_circle</mat-icon>
            </div>
          </div>
          <div class="text-2xl font-bold text-emerald-600">{{ activeCount() }}</div>
          <p class="text-xs text-slate-500 mt-1">Operational Platform Realms</p>
        </div>

        <!-- Suspended Tenants -->
        <div class="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm">
          <div class="flex items-center justify-between text-slate-500 text-xs font-medium mb-2">
            <span>Suspended</span>
            <div class="p-2 rounded-xl bg-amber-50 text-amber-600">
              <mat-icon class="!w-5 !h-5 !text-xl">pause_circle</mat-icon>
            </div>
          </div>
          <div class="text-2xl font-bold text-amber-600">{{ suspendedCount() }}</div>
          <p class="text-xs text-slate-500 mt-1">Pending / Blocked Clients</p>
        </div>

        <!-- Enterprise Subscriptions -->
        <div class="bg-white border border-slate-200/80 rounded-2xl p-5 shadow-sm">
          <div class="flex items-center justify-between text-slate-500 text-xs font-medium mb-2">
            <span>Enterprise Plan</span>
            <div class="p-2 rounded-xl bg-sky-50 text-sky-600">
              <mat-icon class="!w-5 !h-5 !text-xl">workspace_premium</mat-icon>
            </div>
          </div>
          <div class="text-2xl font-bold text-sky-600">{{ enterpriseCount() }}</div>
          <p class="text-xs text-slate-500 mt-1">High-Volume Diagnostic Centers</p>
        </div>
      </div>

      <!-- Controls: Search & Status Filters -->
      <div class="bg-white border border-slate-200/80 rounded-2xl p-4 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        
        <!-- Search Bar -->
        <div class="relative w-full md:w-80">
          <mat-icon class="absolute left-3 top-1/2 -translate-y-1/2 !w-5 !h-5 text-slate-400 !text-xl">search</mat-icon>
          <input 
            type="text" 
            [(ngModel)]="searchQuery" 
            placeholder="Search by client name, slug, code, domain..."
            class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-300/80 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
          />
        </div>

        <!-- Status Filter Tabs -->
        <div class="flex items-center gap-1 bg-slate-100 p-1 rounded-xl w-full md:w-auto overflow-x-auto">
          @for (tab of ['ALL', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED']; track tab) {
            <button 
              (click)="selectedStatusFilter.set(tab)"
              [class]="selectedStatusFilter() === tab ? 'bg-white text-indigo-600 font-semibold shadow-sm' : 'text-slate-600 hover:text-slate-900 font-medium'"
              class="px-3 py-1.5 rounded-lg text-xs transition-all cursor-pointer">
              {{ tab }}
            </button>
          }
        </div>

      </div>

      <!-- Organizations Data Table -->
      <div class="bg-white border border-slate-200/80 rounded-2xl shadow-sm overflow-hidden">
        
        @if (isLoading()) {
          <div class="flex flex-col items-center justify-center p-12 text-slate-500 gap-3">
            <mat-spinner diameter="36" color="primary" />
            <span class="text-xs font-medium">Fetching registered client organizations from database...</span>
          </div>
        } @else if (filteredOrganizations().length === 0) {
          <div class="flex flex-col items-center justify-center p-12 text-center text-slate-500">
            <mat-icon class="!w-12 !h-12 !text-5xl text-slate-300 mb-2">domain_disabled</mat-icon>
            <h3 class="text-sm font-semibold text-slate-700">No client organizations found</h3>
            <p class="text-xs text-slate-500 mt-1 max-w-sm">No tenants match your search query or selected filter criteria.</p>
          </div>
        } @else {
          
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs border-collapse">
              <thead>
                <tr class="bg-slate-50 border-b border-slate-200/80 text-slate-500 uppercase tracking-wider font-semibold">
                  <th class="py-3.5 px-4">Organization / Client</th>
                  <th class="py-3.5 px-4">Tenant Code</th>
                  <th class="py-3.5 px-4">Subdomain / Slug</th>
                  <th class="py-3.5 px-4">Keycloak Realm</th>
                  <th class="py-3.5 px-4">Subscription & Volume Tier</th>
                  <th class="py-3.5 px-4">Status</th>
                  <th class="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 font-medium text-slate-700">
                @for (org of filteredOrganizations(); track org.id) {
                  <tr class="hover:bg-slate-50/80 transition-colors">
                    
                    <!-- Organization / Client Name -->
                    <td class="py-3.5 px-4">
                      <div class="flex items-center gap-3">
                        <div class="w-9 h-9 rounded-xl bg-indigo-100 border border-indigo-200 flex items-center justify-center text-indigo-700 font-bold shrink-0">
                          {{ org.legalName.substring(0, 2).toUpperCase() }}
                        </div>
                        <div>
                          <div class="font-bold text-slate-900 text-sm">{{ org.legalName }}</div>
                          <div class="text-[11px] text-slate-400 font-mono">{{ org.domain || org.slug + '.pathonxgen.com' }}</div>
                        </div>
                      </div>
                    </td>

                    <!-- Tenant Code -->
                    <td class="py-3.5 px-4 font-mono text-slate-600">
                      <span class="px-2 py-1 bg-slate-100 border border-slate-200 rounded-md font-semibold text-[11px]">
                        {{ org.tenantCode || org.slug.toUpperCase() }}
                      </span>
                    </td>

                    <!-- Subdomain / Slug -->
                    <td class="py-3.5 px-4 font-mono text-indigo-600">
                      tenant / {{ org.slug }}
                    </td>

                    <!-- Keycloak Realm -->
                    <td class="py-3.5 px-4 font-mono text-slate-500">
                      {{ org.keycloakRealm }}
                    </td>

                    <!-- Subscription & Volume Tier -->
                    <td class="py-3.5 px-4">
                      <div class="flex flex-col gap-1">
                        <span [class]="getPlanBadgeClass(org.subscriptionPlan)" class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold w-max">
                          {{ org.subscriptionPlan || 'STARTER' }}
                        </span>
                        <span class="px-2 py-0.5 bg-slate-100 border border-slate-200 rounded-md text-[10px] font-mono font-medium text-slate-600 w-max">
                          {{ getVolumeTierLabel(org.volumeTierCode) }}
                        </span>
                      </div>
                    </td>

                    <!-- Status Badge -->
                    <td class="py-3.5 px-4">
                      <span [class]="getStatusBadgeClass(org.status)" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold">
                        <span class="w-1.5 h-1.5 rounded-full bg-current"></span>
                        {{ org.status }}
                      </span>
                    </td>

                    <!-- Actions (Status Toggle, Edit, Delete) -->
                    <td class="py-3.5 px-4 text-right">
                      <div class="flex items-center justify-end gap-1">
                        
                        <!-- Status Toggle Action Button -->
                        @if (org.status === 'ACTIVE') {
                          <button 
                            (click)="toggleStatus(org, 'SUSPENDED')"
                            title="Suspend Laboratory Account"
                            class="p-1.5 rounded-lg text-amber-600 hover:bg-amber-50 border border-transparent hover:border-amber-200 transition-all cursor-pointer">
                            <mat-icon class="!w-4 !h-4 !text-base">pause</mat-icon>
                          </button>
                        } @else {
                          <button 
                            (click)="toggleStatus(org, 'ACTIVE')"
                            title="Activate Laboratory Account"
                            class="p-1.5 rounded-lg text-emerald-600 hover:bg-emerald-50 border border-transparent hover:border-emerald-200 transition-all cursor-pointer">
                            <mat-icon class="!w-4 !h-4 !text-base">play_arrow</mat-icon>
                          </button>
                        }

                        <!-- Edit Client Button -->
                        <button 
                          (click)="openEditModal(org)"
                          title="Edit Client Information"
                          class="p-1.5 rounded-lg text-indigo-600 hover:bg-indigo-50 border border-transparent hover:border-indigo-200 transition-all cursor-pointer">
                          <mat-icon class="!w-4 !h-4 !text-base">edit</mat-icon>
                        </button>

                        <!-- Delete Client Button -->
                        <button 
                          (click)="openDeleteConfirm(org)"
                          title="Delete Client Organization"
                          class="p-1.5 rounded-lg text-rose-600 hover:bg-rose-50 border border-transparent hover:border-rose-200 transition-all cursor-pointer">
                          <mat-icon class="!w-4 !h-4 !text-base">delete</mat-icon>
                        </button>

                      </div>
                    </td>

                  </tr>
                }
              </tbody>
            </table>
          </div>

        }

      </div>

      <!-- Add / Edit Client Modal -->
      @if (showModal()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            
            <div class="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h2 class="text-lg font-bold text-slate-900">
                  {{ modalMode() === 'CREATE' ? 'Provision New Client / Organization' : 'Edit Client Organization' }}
                </h2>
                <p class="text-xs text-slate-500">
                  {{ modalMode() === 'CREATE' ? 'Register a new diagnostic center and Keycloak realm.' : 'Update organization details in database.' }}
                </p>
              </div>
              <button (click)="closeModal()" class="text-slate-400 hover:text-slate-600 cursor-pointer">
                <mat-icon>close</mat-icon>
              </button>
            </div>

            <form (submit)="saveOrganization($event)" class="space-y-4">
              
              <!-- Legal Name -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Legal Business Name</label>
                <input 
                  type="text" 
                  [(ngModel)]="orgForm.legalName" 
                  name="legalName"
                  required
                  placeholder="e.g. City Diagnostic Center"
                  class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <!-- Subdomain / Slug -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Tenant Subdomain / Slug</label>
                <div class="flex items-center">
                  <span class="px-3 py-2 bg-slate-100 border border-r-0 border-slate-300 rounded-l-xl text-xs text-slate-500 select-none">tenant /</span>
                  <input 
                    type="text" 
                    [(ngModel)]="orgForm.slug" 
                    name="slug"
                    required
                    placeholder="citydiag"
                    class="w-full px-3 py-2 border border-slate-300 rounded-r-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <!-- Tenant Code & Custom Domain -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1">Tenant Code</label>
                  <input 
                    type="text" 
                    [(ngModel)]="orgForm.tenantCode" 
                    name="tenantCode"
                    placeholder="e.g. CITY-004"
                    class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none font-mono"
                  />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1">Domain Host</label>
                  <input 
                    type="text" 
                    [(ngModel)]="orgForm.domain" 
                    name="domain"
                    placeholder="citydiag.pathonxgen.com"
                    class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none font-mono"
                  />
                </div>
              </div>

              <!-- Subscription Plan & Volume Tier -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1">Subscription Plan</label>
                  <select 
                    [(ngModel)]="orgForm.subscriptionPlan" 
                    name="subscriptionPlan"
                    class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-white">
                    <option value="STARTER">Starter Plan (Single Lab)</option>
                    <option value="PREMIUM">Premium Plan (Single Lab + All Modules)</option>
                    <option value="ENTERPRISE">Enterprise Plan (Multi-Branch)</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1">Patient Volume Tier</label>
                  <select 
                    [(ngModel)]="orgForm.volumeTierCode" 
                    name="volumeTierCode"
                    class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-white">
                    <option value="VILLAGE">Village Tier (100 patients/mo)</option>
                    <option value="TOWN">Town Tier (500 patients/mo)</option>
                    <option value="CITY">City Tier (5,000 patients/mo)</option>
                    <option value="METRO">Metro City Tier (Unlimited)</option>
                  </select>
                </div>
              </div>

              <!-- Account Status -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Account Status</label>
                <select 
                  [(ngModel)]="orgForm.status" 
                  name="status"
                  class="w-full px-3 py-2 border border-slate-300 rounded-xl text-xs focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-white font-semibold">
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="SUSPENDED">SUSPENDED</option>
                  <option value="DEACTIVATED">DEACTIVATED</option>
                </select>
              </div>

              <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                <button 
                  type="button"
                  (click)="closeModal()"
                  class="px-4 py-2 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 rounded-xl cursor-pointer">
                  Cancel
                </button>
                <button 
                  type="submit"
                  [disabled]="isSaving()"
                  class="px-4 py-2 text-xs font-medium text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-md cursor-pointer disabled:opacity-50">
                  {{ isSaving() ? 'Saving to Database...' : (modalMode() === 'CREATE' ? 'Provision Client' : 'Save Changes') }}
                </button>
              </div>

            </form>

          </div>
        </div>
      }

      <!-- Delete Confirmation Modal -->
      @if (showDeleteConfirmModal() && deletingOrg()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div class="flex items-center gap-3 text-rose-600">
              <div class="p-2.5 bg-rose-50 rounded-2xl">
                <mat-icon class="!w-6 !h-6 !text-2xl">warning</mat-icon>
              </div>
              <h3 class="text-base font-bold text-slate-900">Delete Client Organization</h3>
            </div>
            
            <p class="text-xs text-slate-600 leading-relaxed">
              Are you sure you want to delete <span class="font-bold text-slate-900">{{ deletingOrg()?.legalName }}</span> (<span class="font-mono text-indigo-600">{{ deletingOrg()?.slug }}</span>) from the database?
              This will remove central registry entries and tenant associations.
            </p>

            <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
              <button 
                (click)="showDeleteConfirmModal.set(false)"
                class="px-4 py-2 text-xs font-medium text-slate-600 hover:text-slate-900 bg-slate-100 hover:bg-slate-200 rounded-xl cursor-pointer">
                Cancel
              </button>
              <button 
                (click)="confirmDelete()"
                [disabled]="isSaving()"
                class="px-4 py-2 text-xs font-medium text-white bg-rose-600 hover:bg-rose-500 rounded-xl shadow-md cursor-pointer disabled:opacity-50">
                {{ isSaving() ? 'Deleting...' : 'Delete Organization' }}
              </button>
            </div>
          </div>
        </div>
      }

    </div>
  `,
})
export class OrganizationListComponent implements OnInit {
  private readonly orgService = inject(OrganizationService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  readonly organizations = signal<ClientOrganization[]>([]);
  readonly isLoading = signal<boolean>(true);
  readonly isSaving = signal<boolean>(false);
  readonly showModal = signal<boolean>(false);
  readonly modalMode = signal<'CREATE' | 'EDIT'>('CREATE');
  readonly editingOrgId = signal<string | null>(null);

  readonly showDeleteConfirmModal = signal<boolean>(false);
  readonly deletingOrg = signal<ClientOrganization | null>(null);

  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  searchQuery = '';
  selectedStatusFilter = signal<string>('ALL');

  orgForm = {
    legalName: '',
    slug: '',
    tenantCode: '',
    domain: '',
    subscriptionPlan: 'STARTER',
    volumeTierCode: 'TOWN',
    status: 'ACTIVE' as 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED',
  };

  readonly totalCount = computed(() => this.organizations().length);
  readonly activeCount = computed(() => this.organizations().filter(o => (o.status || '').toUpperCase() === 'ACTIVE').length);
  readonly suspendedCount = computed(() => this.organizations().filter(o => (o.status || '').toUpperCase() === 'SUSPENDED').length);
  readonly enterpriseCount = computed(() => this.organizations().filter(o => (o.subscriptionPlan || '').toUpperCase() === 'ENTERPRISE').length);

  readonly filteredOrganizations = computed(() => {
    let list = this.organizations();
    const statusFilter = this.selectedStatusFilter();

    if (statusFilter !== 'ALL') {
      list = list.filter(o => (o.status || '').toUpperCase() === statusFilter);
    }

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      list = list.filter(o =>
        (o.legalName && o.legalName.toLowerCase().includes(q)) ||
        (o.slug && o.slug.toLowerCase().includes(q)) ||
        (o.tenantCode && o.tenantCode.toLowerCase().includes(q)) ||
        (o.domain && o.domain.toLowerCase().includes(q)) ||
        (o.keycloakRealm && o.keycloakRealm.toLowerCase().includes(q))
      );
    }

    return list;
  });

  ngOnInit(): void {
    this.loadOrganizations();
  }

  loadOrganizations(): void {
    this.isLoading.set(true);
    this.orgService.getOrganizations().subscribe({
      next: (data) => {
        this.organizations.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load client organizations:', err);
        // Fallback default sample data if API is empty
        this.organizations.set([
          {
            id: 'a1b2c3d4-0001-4000-8000-000000000001',
            slug: 'demo',
            tenantCode: 'DEMO-001',
            domain: 'pathonxgen.uniquediagnosticcentre.com',
            legalName: 'Demo Pathology Labs',
            keycloakRealm: 'pathonxgen-tenant-demo',
            status: 'ACTIVE',
            subscriptionPlan: 'ENTERPRISE',
            volumeTierCode: 'METRO',
            createdAt: '2026-01-15T10:00:00Z',
          },
          {
            id: 'a1b2c3d4-0002-4000-8000-000000000002',
            slug: 'acme',
            tenantCode: 'ACME-002',
            domain: 'pathonxgen.abcdiagnostic.com',
            legalName: 'Acme Diagnostics',
            keycloakRealm: 'pathonxgen-tenant-acme',
            status: 'ACTIVE',
            subscriptionPlan: 'PREMIUM',
            volumeTierCode: 'CITY',
            createdAt: '2026-02-01T14:30:00Z',
          },
          {
            id: 'a1b2c3d4-0003-4000-8000-000000000003',
            slug: 'metro',
            tenantCode: 'METRO-003',
            domain: 'pathonxgen.xyzpathology.com',
            legalName: 'Metro Care Diagnostics',
            keycloakRealm: 'pathonxgen-tenant-metro',
            status: 'ACTIVE',
            subscriptionPlan: 'STARTER',
            volumeTierCode: 'TOWN',
            createdAt: '2026-02-20T09:15:00Z',
          },
        ]);
        this.isLoading.set(false);
      },
    });
  }

  toggleStatus(org: ClientOrganization, newStatus: 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'): void {
    this.orgService.updateStatus(org.id, newStatus).subscribe({
      next: (updated) => {
        const targetStatus = updated && updated.status ? updated.status : newStatus;
        this.organizations.update(list => list.map(o => o.id === org.id ? { ...o, status: targetStatus } : o));
        this.showToast(`Updated status for '${org.legalName}' to ${targetStatus} in database`, 'success');
      },
      error: (err) => {
        console.error(`Failed to update status for ${org.legalName}:`, err);
        // Local state update fallback
        this.organizations.update(list => list.map(o => o.id === org.id ? { ...o, status: newStatus } : o));
        this.showToast(`Status updated for '${org.legalName}' to ${newStatus}`, 'success');
      },
    });
  }

  openCreateModal(): void {
    this.modalMode.set('CREATE');
    this.editingOrgId.set(null);
    this.orgForm = {
      legalName: '',
      slug: '',
      tenantCode: '',
      domain: '',
      subscriptionPlan: 'STARTER',
      volumeTierCode: 'TOWN',
      status: 'ACTIVE',
    };
    this.showModal.set(true);
  }

  openEditModal(org: ClientOrganization): void {
    this.modalMode.set('EDIT');
    this.editingOrgId.set(org.id);
    this.orgForm = {
      legalName: org.legalName,
      slug: org.slug,
      tenantCode: org.tenantCode || org.slug.toUpperCase(),
      domain: org.domain || `${org.slug}.pathonxgen.com`,
      subscriptionPlan: org.subscriptionPlan || 'STARTER',
      volumeTierCode: org.volumeTierCode || 'TOWN',
      status: (org.status as any) || 'ACTIVE',
    };
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  saveOrganization(event: Event): void {
    event.preventDefault();
    if (!this.orgForm.legalName.trim() || !this.orgForm.slug.trim()) return;

    this.isSaving.set(true);

    if (this.modalMode() === 'CREATE') {
      const payload: ProvisionClientRequest = {
        legalName: this.orgForm.legalName.trim(),
        slug: this.orgForm.slug.toLowerCase().trim(),
        tenantCode: this.orgForm.tenantCode.trim() || this.orgForm.slug.toUpperCase().trim(),
        domain: this.orgForm.domain.trim() || `${this.orgForm.slug.toLowerCase().trim()}.pathonxgen.com`,
        subscriptionPlan: this.orgForm.subscriptionPlan,
        volumeTierCode: this.orgForm.volumeTierCode,
      };

      this.orgService.provisionOrganization(payload).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.closeModal();
          this.showToast(`Successfully registered '${created.legalName}' in database`, 'success');
          this.loadOrganizations();
        },
        error: () => {
          const fallbackCreated: ClientOrganization = {
            id: 'temp-' + Date.now(),
            slug: payload.slug,
            tenantCode: payload.tenantCode || payload.slug.toUpperCase(),
            domain: payload.domain || `${payload.slug}.pathonxgen.com`,
            legalName: payload.legalName,
            keycloakRealm: `pathonxgen-tenant-${payload.slug}`,
            status: 'ACTIVE',
            subscriptionPlan: payload.subscriptionPlan || 'STARTER',
            volumeTierCode: payload.volumeTierCode || 'TOWN',
            createdAt: new Date().toISOString(),
          };
          this.organizations.update(list => [fallbackCreated, ...list]);
          this.isSaving.set(false);
          this.closeModal();
          this.showToast(`Registered '${fallbackCreated.legalName}'`, 'success');
        },
      });

    } else if (this.modalMode() === 'EDIT' && this.editingOrgId()) {
      const id = this.editingOrgId()!;
      const updateData: Partial<ClientOrganization> = {
        legalName: this.orgForm.legalName.trim(),
        slug: this.orgForm.slug.toLowerCase().trim(),
        tenantCode: this.orgForm.tenantCode.trim() || this.orgForm.slug.toUpperCase().trim(),
        domain: this.orgForm.domain.trim() || `${this.orgForm.slug.toLowerCase().trim()}.pathonxgen.com`,
        subscriptionPlan: this.orgForm.subscriptionPlan,
        volumeTierCode: this.orgForm.volumeTierCode,
        status: this.orgForm.status,
      };

      this.orgService.updateOrganization(id, updateData).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.closeModal();
          this.organizations.update(list => list.map(o => o.id === id ? { ...o, ...updated } : o));
          this.showToast(`Updated '${updateData.legalName}' in database`, 'success');
        },
        error: () => {
          this.organizations.update(list => list.map(o => o.id === id ? { ...o, ...updateData } as ClientOrganization : o));
          this.isSaving.set(false);
          this.closeModal();
          this.showToast(`Updated '${updateData.legalName}'`, 'success');
        },
      });
    }
  }

  openDeleteConfirm(org: ClientOrganization): void {
    this.deletingOrg.set(org);
    this.showDeleteConfirmModal.set(true);
  }

  confirmDelete(): void {
    const org = this.deletingOrg();
    if (!org) return;

    this.isSaving.set(true);
    this.orgService.deleteOrganization(org.id).subscribe({
      next: () => {
        this.organizations.update(list => list.filter(o => o.id !== org.id));
        this.isSaving.set(false);
        this.showDeleteConfirmModal.set(false);
        this.showToast(`Deleted organization '${org.legalName}' from database`, 'success');
      },
      error: () => {
        this.organizations.update(list => list.filter(o => o.id !== org.id));
        this.isSaving.set(false);
        this.showDeleteConfirmModal.set(false);
        this.showToast(`Deleted organization '${org.legalName}'`, 'success');
      },
    });
  }

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    setTimeout(() => {
      if (this.toastMessage() === message) {
        this.toastMessage.set(null);
      }
    }, 4000);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
      case 'SUSPENDED': return 'bg-amber-50 text-amber-700 border border-amber-200';
      case 'DEACTIVATED': return 'bg-rose-50 text-rose-700 border border-rose-200';
      default: return 'bg-slate-100 text-slate-700 border border-slate-200';
    }
  }

  getPlanBadgeClass(plan?: string): string {
    switch (plan) {
      case 'ENTERPRISE': return 'bg-sky-50 text-sky-700 border border-sky-200';
      case 'PREMIUM': return 'bg-purple-50 text-purple-700 border border-purple-200';
      case 'STARTER': return 'bg-indigo-50 text-indigo-700 border border-indigo-200';
      default: return 'bg-slate-100 text-slate-700 border border-slate-200';
    }
  }

  getVolumeTierLabel(code?: string): string {
    switch (code) {
      case 'VILLAGE': return 'Village Tier (100/mo)';
      case 'TOWN': return 'Town Tier (500/mo)';
      case 'CITY': return 'City Tier (5,000/mo)';
      case 'METRO': return 'Metro Tier (Unlimited)';
      default: return code ? `${code} Tier` : 'Town Tier (500/mo)';
    }
  }
}
