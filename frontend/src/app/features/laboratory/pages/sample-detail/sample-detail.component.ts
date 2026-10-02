import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DatePipe, NgClass } from '@angular/common';
import { SampleStore } from '../../store/sample.store';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-sample-detail',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    NgClass,
    DatePipe,
  ],
  templateUrl: './sample-detail.component.html',
  styleUrl: './sample-detail.component.css',
})
export class SampleDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly sampleStore = inject(SampleStore);
  protected readonly authStore = inject(AuthStore);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.sampleStore.loadSample(id);
  }

  goBack(): void {
    this.router.navigate(['/samples']);
  }

  navigateToTransition(): void {
    const sample = this.sampleStore.selectedSample();
    if (!sample) return;
    const firstTransition = this.sampleStore.availableTransitions()[0];
    if (firstTransition) {
      this.router.navigate(['/samples', sample.id, 'transition', firstTransition.status]);
    }
  }

  async handleTransition(targetStatus: string, actorRole: string): Promise<void> {
    const sample = this.sampleStore.selectedSample();
    if (!sample) return;
    try {
      await this.sampleStore.transitionSample(sample.id, targetStatus, { actorRole });
      this.snackBar.open(`Transitioned to ${targetStatus}`, 'Close', { duration: 3000 });
      this.router.navigate(['/samples']);
    } catch {
      this.snackBar.open('Transition failed. Please try again.', 'Close', {
        duration: 5000,
        panelClass: ['snack-error'],
      });
    }
  }

  statusClass(status: string): string {
    if (status === 'REJECTED') return 'bg-red-100 text-red-800';
    if (status === 'APPROVED' || status === 'REPORT_DELIVERED') return 'bg-green-100 text-green-800';
    if (status === 'VERIFIED') return 'bg-blue-100 text-blue-800';
    return 'bg-gray-100 text-gray-800';
  }
}
