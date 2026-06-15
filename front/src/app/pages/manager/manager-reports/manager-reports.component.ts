// manager-reports.component.ts
import {
  Component,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ManagerService } from '../manager-dashboard/services/manager.service';
import { ManagerDashboardResponseDTO } from '../manager-dashboard/models/manager-dashboard.model';

@Component({
  selector: 'app-manager-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manager-reports.component.html',
  styleUrls: ['./manager-reports.component.css']
})
export class ManagerReportsComponent implements OnInit {
  report: ManagerDashboardResponseDTO | null = null;
  loading = true;
  error: string | null = null;
  exporting = false;
  today = new Date();
  selectedDate: string = this.formatDate(new Date());

  constructor(
    private managerService: ManagerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadReport();
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  loadReport(): void {
    this.loading = true;
    this.error = null;
    this.cdr.markForCheck();

    this.managerService.getRapportJournalier(this.selectedDate).subscribe({
      next: (data) => {
        this.report = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du rapport';
        this.loading = false;
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }

  onDateChange(): void {
    this.loadReport();
  }

  export(format: 'csv' | 'pdf'): void {
    this.exporting = true;
    this.error = null;
    this.cdr.markForCheck();

    this.managerService.exportRapport(format, this.selectedDate).subscribe({
      next: (blob: Blob) => {
        const filename = `rapport_agence_${this.selectedDate}.${format}`;
        const url = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = filename;

        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        window.URL.revokeObjectURL(url);

        this.exporting = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = `Erreur lors de l'export ${format.toUpperCase()}`;
        this.exporting = false;
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }
}
