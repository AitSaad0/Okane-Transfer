// manager-transfers.component.ts
import {
  Component,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ManagerService } from '../manager-dashboard/services/manager.service';
import {
  PageResponseDto,
  TransfertResponseDTO
} from '../manager-dashboard/models/manager-dashboard.model';

@Component({
  selector: 'app-manager-transfers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manager-transfers.component.html',
  styleUrls: ['./manager-transfers.component.css']
})
export class ManagerTransfersComponent implements OnInit {
  transferts: TransfertResponseDTO[] = [];
  loading = true;
  error: string | null = null;

  selectedStatut = '';
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;

  statutOptions = ['EN_ATTENTE', 'PAYE', 'ANNULE', 'REJETE'];

  constructor(
    private managerService: ManagerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTransferts();
  }

  loadTransferts(): void {
    this.loading = true;
    this.error = null;
    this.cdr.markForCheck();

    const statut = this.selectedStatut || undefined;

    this.managerService
      .getTransfertsAgence(statut, this.page, this.size)
      .subscribe({
        next: (data: PageResponseDto<TransfertResponseDTO>) => {
          this.transferts = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.error = 'Erreur lors du chargement des transferts';
          this.loading = false;
          this.cdr.markForCheck();
          console.error(err);
        }
      });
  }

  onFilterChange(): void {
    this.page = 0;
    this.loadTransferts();
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadTransferts();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadTransferts();
    }
  }

  statutClass(statut: string): string {
    switch (statut) {
      case 'PAYE':
        return 'badge-success';
      case 'EN_ATTENTE':
        return 'badge-warning';
      case 'ANNULE':
      case 'REJETE':
        return 'badge-danger';
      default:
        return 'badge-default';
    }
  }
  parseDate(dateValue: any): Date | null {
    if (!dateValue) {
      return null;
    }

    if (Array.isArray(dateValue)) {
      return new Date(
        dateValue[0],      // year
        dateValue[1] - 1,  // month
        dateValue[2],      // day
        dateValue[3] || 0, // hour
        dateValue[4] || 0, // minute
        dateValue[5] || 0  // second
      );
    }

    const parts = String(dateValue).split(',').map(Number);

    return new Date(
      parts[0],
      parts[1] - 1,
      parts[2],
      parts[3] || 0,
      parts[4] || 0,
      parts[5] || 0
    );
  }
}
