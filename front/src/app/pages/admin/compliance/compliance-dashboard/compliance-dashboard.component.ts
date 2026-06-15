import { TranslateModule } from '@ngx-translate/core';
import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface ComplianceDashboardDto {
  totalTransfers: number;
  suspiciousTransfers: number;
  totalSarReports: number;
  openSarReports: number;
  suspiciousRate: number;
}

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './compliance-dashboard.component.html',
  styleUrls: ['./compliance-dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComplianceDashboardComponent implements OnInit {
  dashboard: ComplianceDashboardDto | null = null;
  loading = false;
  error: string | null = null;

  private readonly apiUrl = '/api/v1/admin/compliance/dashboard';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;
    this.cdr.markForCheck();

    this.http.get<ComplianceDashboardDto>(this.apiUrl).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
