import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PatientStore } from '../../store/patient.store';

@Component({
  selector: 'app-patient-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './patient-register.component.html',
  styleUrl: './patient-register.component.css',
})
export class PatientRegisterComponent {
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly patientStore = inject(PatientStore);

  /** Reactive form — replaces react-hook-form + Zod */
  readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(1)]],
    dateOfBirth: ['', Validators.required],
    gender: ['MALE', Validators.required],
    phone: [''],
    email: ['', Validators.email],
    address: [''],
    insuranceProvider: [''],
    corporateAccountCode: [''],
  });

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;

    try {
      await this.patientStore.registerPatient(this.form.value as any);
      this.snackBar.open('Patient registered successfully!', 'Close', { duration: 3000 });
      this.router.navigate(['/patients']);
    } catch {
      this.snackBar.open('Failed to register patient. Please try again.', 'Close', {
        duration: 5000,
        panelClass: ['snack-error'],
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/patients']);
  }
}
