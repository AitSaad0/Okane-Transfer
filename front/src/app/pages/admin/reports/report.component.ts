import { Component, ChangeDetectorRef } from '@angular/core';
import { ReportService } from './services/report.service';
import { DailyReportDto, MonthlyReportDto, CorridorPerformanceDto } from './models/report.model';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./report.component.css'],
})
export class ReportComponent {
  reportType: 'daily' | 'monthly' | 'corridor' = 'daily';

  date: string = new Date().toISOString().split('T')[0];
  year: number = new Date().getFullYear();
  startDate: string = '';
  endDate: string = '';

  corridorId?: string;
  agenceId?: string;

  data: any[] = [];
  loading = false;
  error: string = '';

  constructor(
    private reportService: ReportService,
    private cdr: ChangeDetectorRef       // ← inject this
  ) {}

  private setData(res: any[]) {
    this.data = [...res];
    this.loading = false;
    this.cdr.detectChanges();            // ← force view update
  }

  private setError(msg: string) {
    this.error = msg;
    this.loading = false;
    this.cdr.detectChanges();
  }

  generateReport() {
    this.error = '';

    if (this.reportType === 'daily' && !this.date) {
      this.error = 'Please select a date.'; return;
    }
    if (this.reportType === 'monthly' && !this.year) {
      this.error = 'Please enter a year.'; return;
    }
    if (this.reportType === 'corridor' && (!this.startDate || !this.endDate)) {
      this.error = 'Please select both start and end dates.'; return;
    }

    this.loading = true;
    this.data = [];

    if (this.reportType === 'daily') {
      this.reportService
        .getDailyReport(this.date, this.corridorId, this.agenceId)
        .subscribe({
          next: (res: DailyReportDto[]) => this.setData(res),
          error: () => this.setError('Failed to load daily report.')
        });
    }

    if (this.reportType === 'monthly') {
      this.reportService.getMonthlyReport(this.year).subscribe({
        next: (res: MonthlyReportDto[]) => this.setData(res),
        error: () => this.setError('Failed to load monthly report.')
      });
    }

    if (this.reportType === 'corridor') {
      this.reportService
        .getCorridorReport(this.startDate, this.endDate)
        .subscribe({
          next: (res: CorridorPerformanceDto[]) => this.setData(res),
          error: () => this.setError('Failed to load corridor report.')
        });
    }
  }

  onReportTypeChange() {
    this.data = [];
    this.error = '';
  }

  export(format: 'csv' | 'pdf') {
    if (this.reportType !== 'daily' || !this.date) return;

    this.reportService.exportReport(format, this.date, this.corridorId).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${this.reportType}.${format}`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  getKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }
}
