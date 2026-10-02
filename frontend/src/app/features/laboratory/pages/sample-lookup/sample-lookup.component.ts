import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NgClass } from '@angular/common';
import { SampleStore } from '../../store/sample.store';

@Component({
  selector: 'app-sample-lookup',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    NgClass,
  ],
  templateUrl: './sample-lookup.component.html',
  styleUrl: './sample-lookup.component.css',
})
export class SampleLookupComponent {
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  protected readonly sampleStore = inject(SampleStore);

  readonly form = this.fb.group({
    barcode: ['', Validators.required],
  });

  submitted = signal(false);

  async onSearch(): Promise<void> {
    if (this.form.invalid) return;
    const barcode = this.form.value.barcode ?? '';
    this.submitted.set(true);
    await this.sampleStore.lookupByBarcode(barcode);
  }

  viewDetails(): void {
    const sample = this.sampleStore.barcodeResult();
    if (sample) this.router.navigate(['/samples', sample.id]);
  }

  clearSearch(): void {
    this.form.reset();
    this.submitted.set(false);
    this.sampleStore.clearBarcodeResult();
  }

  statusClass(status: string): string {
    if (status === 'REJECTED') return 'text-red-600';
    if (status === 'APPROVED' || status === 'REPORT_DELIVERED') return 'text-green-600';
    return 'text-gray-800';
  }
}
