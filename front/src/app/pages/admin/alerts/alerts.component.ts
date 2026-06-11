import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';

export interface AlertDto {
  type: string;
  message: string;
  severity: string;
  timestamp: number[] | string;
}

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts.component.html',
  styleUrls: ['./alerts.component.css']
})
export class AlertsComponent implements OnInit, OnDestroy {

  private readonly API_URL = '/api/v1/admin/reports/alerts';

  alerts: AlertDto[] = [];
  loading = false;
  error = '';

  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('AlertsComponent INIT');
    this.loadAlerts();
  }

  private loadAlerts(): void {
    this.loading = true;
    this.error = '';
    this.alerts = [];

    this.http
      .get<AlertDto[]>(this.API_URL)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          console.log('Request finalized');
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Alerts received:', data);

          this.alerts = Array.isArray(data) ? data : [];

          console.log('Alert count:', this.alerts.length);
        },

        error: (err) => {
          console.error('Alerts error:', err);
          this.error = 'Erreur de chargement';
        }
      });
  }

  getSeverityClass(severity: string): string {
    const map: Record<string, string> = {
      HIGH: 'badge-high',
      MEDIUM: 'badge-medium',
      LOW: 'badge-low'
    };

    return map[severity] ?? 'badge-low';
  }

  getTypeIcon(type: string): string {
    const map: Record<string, string> = {
      WATCHLIST_MATCH: '⚠',
      INVALID_DOCUMENT: '📄',
      KYC_INCOMPLETE: '🪪',
      DOCUMENT_EXPIRING: '⏳'
    };

    return map[type] ?? '🔔';
  }

  parseTimestamp(ts: number[] | string): Date {
    if (Array.isArray(ts)) {
      return new Date(
        ts[0],
        ts[1] - 1,
        ts[2],
        ts[3] ?? 0,
        ts[4] ?? 0,
        ts[5] ?? 0
      );
    }

    return new Date(ts);
  }

  ngOnDestroy(): void {
    console.log('AlertsComponent DESTROY');

    this.destroy$.next();
    this.destroy$.complete();
  }
}
