import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AgGridAngular } from 'ag-grid-angular';
import type { ColDef, RowDoubleClickedEvent } from 'ag-grid-community';
import { AllCommunityModule, ModuleRegistry } from 'ag-grid-community';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { SampleStore } from '../../store/sample.store';
import { Sample } from '../../models/laboratory.models';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
  selector: 'app-sample-list',
  standalone: true,
  imports: [AgGridAngular, MatButtonModule, MatInputModule, MatFormFieldModule, MatIconModule],
  templateUrl: './sample-list.component.html',
  styleUrl: './sample-list.component.css',
})
export class SampleListComponent implements OnInit {
  protected readonly router = inject(Router);
  protected readonly sampleStore = inject(SampleStore);

  searchQuery = signal('');
  currentPage = signal(0);

  readonly columnDefs: ColDef<Sample>[] = [
    { field: 'barcode', headerName: 'Barcode', width: 160 },
    { field: 'testType', headerName: 'Test Type', flex: 1 },
    { field: 'status', headerName: 'Status', width: 160 },
    { field: 'createdAt', headerName: 'Created', width: 180 },
  ];

  readonly defaultColDef: ColDef = { sortable: true, filter: true, resizable: true };

  ngOnInit(): void {
    this.loadSamples();
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.loadSamples();
  }

  onRowDoubleClicked(event: RowDoubleClickedEvent<Sample>): void {
    if (event.data) this.router.navigate(['/samples', event.data.id]);
  }

  previousPage(): void {
    this.currentPage.update((p) => p - 1);
    this.loadSamples();
  }

  nextPage(): void {
    this.currentPage.update((p) => p + 1);
    this.loadSamples();
  }

  private loadSamples(): void {
    this.sampleStore.searchSamples(this.searchQuery(), this.currentPage());
  }
}
