import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SampleStore } from '../../store/sample.store';

@Component({
  selector: 'app-sample-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './sample-register.component.html',
  styleUrl: './sample-register.component.css',
})
export class SampleRegisterComponent {
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly sampleStore = inject(SampleStore);

  readonly form = this.fb.group({
    barcode: ['', Validators.required],
    patientId: ['', Validators.required],
    testType: ['', Validators.required],
  });

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;
    try {
      await this.sampleStore.registerSample(this.form.value as any);
      this.snackBar.open('Sample registered successfully!', 'Close', { duration: 3000 });
      this.router.navigate(['/samples']);
    } catch {
      this.snackBar.open('Failed to register sample. Please try again.', 'Close', {
        duration: 5000,
        panelClass: ['snack-error'],
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/samples']);
  }
}
