<<<<<<< Updated upstream
import {
  Component,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
  OnInit
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
=======
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
>>>>>>> Stashed changes
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-compliance-threshold',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './compliance-threshold.component.html',
  styleUrls: ['./compliance-threshold.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
<<<<<<< Updated upstream
export class ComplianceThresholdsComponent implements OnInit {

  form: ThresholdRequest = {
=======
export class ComplianceThresholdComponent implements OnInit {
  form: any = {
>>>>>>> Stashed changes
    sarThreshold: 0
  };
  loading = false;
  saved = false;
<<<<<<< Updated upstream
  errorMessage: string | null = null;

  private readonly apiUrl =
    '/api/v1/admin/compliance/thresholds';

  private readonly thresholdUrl =
    '/api/v1/admin/compliance/thresholds/latest';
=======
  errorMessage = '';
>>>>>>> Stashed changes

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
<<<<<<< Updated upstream
    this.loadCurrentThreshold();
  }

  loadCurrentThreshold(): void {
    this.http.get<number>(this.thresholdUrl).subscribe({
      next: (threshold) => {
        this.form.sarThreshold = threshold;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Failed to load current threshold.';
        this.cdr.markForCheck();
      }
    });
  }

  onSubmit(): void {
    if (this.loading) return;
=======
    this.loadThreshold();
  }
>>>>>>> Stashed changes

  loadThreshold(): void {
    this.loading = true;
    this.http.get<any>('/api/compliance/config').subscribe({
      next: (data) => {
        if (data) {
          this.form.sarThreshold = data.sarThreshold || 0;
        }
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du chargement du seuil';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

<<<<<<< Updated upstream
    this.http.put(this.apiUrl, this.form, { responseType: 'text' })
      .subscribe({
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
          this.errorMessage =
            err?.error || 'Failed to update threshold.';
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
=======
  onSubmit(): void {
    this.loading = true;
    this.saved = false;
    this.errorMessage = '';
    
    this.http.post('/api/compliance/config', this.form).subscribe({
      next: () => {
        this.loading = false;
        this.saved = true;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.saved = false;
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors de la mise à jour';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
>>>>>>> Stashed changes
  }
}
