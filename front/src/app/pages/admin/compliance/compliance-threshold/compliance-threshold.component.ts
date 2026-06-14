import { Component, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ThresholdRequest {
  sarThreshold: number;
}

@Component({
  selector: 'app-compliance-thresholds',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compliance-threshold.component.html',
  styleUrls: ['./compliance-threshold.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComplianceThresholdsComponent {
  form: ThresholdRequest = {
    sarThreshold: 0
  };

  loading = false;
  saved = false;
  errorMessage: string | null = null;

  private readonly apiUrl = '/api/v1/admin/compliance/thresholds';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    if (this.loading || this.saved) return;

    this.loading = true;
    this.errorMessage = null;
    this.cdr.markForCheck();

    this.http.put(this.apiUrl, this.form, { responseType: 'text' }).subscribe({
      next: () => {
        this.loading = false;
        this.saved = true;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.saved = false;
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (err) => {
        this.errorMessage = err?.error || 'Failed to update threshold.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
