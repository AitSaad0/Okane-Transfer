import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AuditLogService } from './service/audit-logs.service';
import { JournalAudit } from './models/journal-audit.model';

@Component({
  selector: 'app-audit-log-list',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './audit-log-list.component.html',
  styleUrl: './audit-log-list.component.css'
})
export class AuditLogListComponent implements OnInit {

  logs = signal<JournalAudit[]>([]);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  pageSize = 20;
  pageIndex = 0;
  sortField = 'timestamp';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.auditLogService
      .getAll(this.pageIndex, this.pageSize, this.sortField, this.sortDirection)
      .subscribe({
        next: (response) => {
          this.logs.set(response.content);
          this.totalElements.set(response.totalElements);
          this.totalPages.set(response.totalPages);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Erreur lors du chargement des journaux d\'audit', err);
          this.errorMessage.set('Impossible de charger les journaux d\'audit.');
          this.isLoading.set(false);
        }
      });
  }

  // Tri au clic sur l'en-tête de colonne
  sortBy(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.pageIndex = 0;
    this.loadLogs();
  }

  sortIcon(field: string): string {
    if (this.sortField !== field) return '';
    return this.sortDirection === 'asc' ? '▲' : '▼';
  }

  // Pagination
  nextPage(): void {
    if (this.pageIndex + 1 < this.totalPages()) {
      this.pageIndex++;
      this.loadLogs();
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.loadLogs();
    }
  }

  goToPage(index: number): void {
    if (index >= 0 && index < this.totalPages()) {
      this.pageIndex = index;
      this.loadLogs();
    }
  }

  // Petite badge CSS selon le type d'action
  getTypeClass(type: string): string {
    switch (type?.toUpperCase()) {
      case 'ERROR':
      case 'FAILURE':
        return 'badge badge-error';
      case 'WARNING':
        return 'badge badge-warning';
      default:
        return 'badge badge-default';
    }
  }
  formatTimestamp(value: any): Date | null {
    if (Array.isArray(value)) {
      // [année, mois(1-12), jour, heure, minute, seconde, nano?]
      const [year, month, day, hour = 0, minute = 0, second = 0] = value;
      return new Date(year, month - 1, day, hour, minute, second);
    }
    return value ? new Date(value) : null;
  }
}
