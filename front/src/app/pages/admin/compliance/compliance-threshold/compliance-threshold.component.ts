import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
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
export class ComplianceThresholdComponent implements OnInit {
  thresholdAmount: number = 0;
  loading = false;
  saved = false;
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadThreshold();
  }

  loadThreshold(): void {
    this.loading = true;
    this.http.get<number>('/api/v1/admin/compliance/thresholds/latest').subscribe({
      next: (data) => {
        this.thresholdAmount = data ?? 0;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement du seuil';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.saved = false;
    this.errorMessage = '';

    this.http.put('/api/v1/admin/compliance/thresholds', 
      { sarThreshold: this.thresholdAmount },
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.loading = false;
        this.saved = true;
        this.thresholdAmount = this.thresholdAmount;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.saved = false;
          this.cdr.markForCheck();
        }, 3000);
      },
      error: () => {
        this.errorMessage = 'Erreur lors de la mise à jour';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
