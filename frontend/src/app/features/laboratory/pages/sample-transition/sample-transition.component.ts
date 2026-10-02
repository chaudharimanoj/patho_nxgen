import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SampleStore } from '../../store/sample.store';
import { AuthStore } from '../../../../core/auth/auth.store';

const LAB_ROLES = ['PHLEBOTOMIST', 'TECHNICIAN', 'PATHOLOGIST', 'LAB_ADMINISTRATOR'];

@Component({
  selector: 'app-sample-transition',
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
  templateUrl: './sample-transition.component.html',
  styleUrl: './sample-transition.component.css',
})
export class SampleTransitionComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly sampleStore = inject(SampleStore);
  protected readonly authStore = inject(AuthStore);

  readonly form = this.fb.group({ note: [''] });
  targetStatus = '';

  get hasPermission(): boolean {
    return this.authStore.roles().some((r) => LAB_ROLES.includes(r));
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.targetStatus = this.route.snapshot.paramMap.get('status') ?? '';
    this.sampleStore.loadSample(id);
  }

  async onSubmit(): Promise<void> {
    const sample = this.sampleStore.selectedSample();
    if (!sample) return;

    const actorRole =
      this.authStore.roles().find((r) => LAB_ROLES.includes(r)) ?? '';

    try {
      await this.sampleStore.transitionSample(sample.id, this.targetStatus, {
        actorRole,
        note: this.form.value.note ?? undefined,
      });
      this.snackBar.open(`Successfully transitioned to ${this.targetStatus}`, 'Close', {
        duration: 3000,
      });
      setTimeout(() => this.router.navigate(['/samples']), 1500);
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? 'Failed to transition sample';
      this.snackBar.open(msg, 'Close', { duration: 5000, panelClass: ['snack-error'] });
    }
  }

  cancel(): void {
    const sample = this.sampleStore.selectedSample();
    if (sample) this.router.navigate(['/samples', sample.id]);
    else this.router.navigate(['/samples']);
  }
}
