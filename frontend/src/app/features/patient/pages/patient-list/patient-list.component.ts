import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AgGridAngular } from 'ag-grid-angular';
import type { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { AllCommunityModule, ModuleRegistry } from 'ag-grid-community';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { PatientStore } from '../../store/patient.store';
import { Patient } from '../../models/patient.models';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [
    AgGridAngular,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    FormsModule,
  ],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css',
})
export class PatientListComponent implements OnInit {
  private readonly router = inject(Router);
  protected readonly patientStore = inject(PatientStore);

  searchQuery = signal('');
  currentPage = signal(0);

  readonly columnDefs: ColDef<Patient>[] = [
    { field: 'mrn', headerName: 'MRN', width: 140 },
    { field: 'fullName', headerName: 'Name', flex: 1 },
    { field: 'dateOfBirth', headerName: 'DOB', width: 120 },
    { field: 'gender', headerName: 'Gender', width: 110 },
    { field: 'phone', headerName: 'Phone', width: 140 },
  ];

  readonly defaultColDef: ColDef = {
    sortable: true,
    filter: true,
    resizable: true,
  };

  ngOnInit(): void {
    this.loadPatients();
  }

  onGridReady(_event: GridReadyEvent): void {
    // Grid ready — data already loaded via ngOnInit
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.loadPatients();
  }

  onRowDoubleClicked(event: RowDoubleClickedEvent<Patient>): void {
    if (event.data) {
      this.router.navigate(['/patients', event.data.id]);
    }
  }

  previousPage(): void {
    this.currentPage.update((p) => p - 1);
    this.loadPatients();
  }

  nextPage(): void {
    this.currentPage.update((p) => p + 1);
    this.loadPatients();
  }

  navigateToRegister(): void {
    this.router.navigate(['/patients/new']);
  }

  private loadPatients(): void {
    this.patientStore.loadPatients(this.searchQuery(), this.currentPage());
  }
}
