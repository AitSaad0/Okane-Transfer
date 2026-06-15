import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ClientTransferService } from '../../../../core/services/client-transfer.service';
import { Transfer, TransferStatus, TransferSearchParams } from '../models/client-transfer.model';

@Component({
  selector: 'app-client-transfers',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './transfer-list.component.html',
  styleUrls: ['./transfer-list.component.css'],
})
export class ClientTransfersComponent implements OnInit {
  transfers = signal<Transfer[]>([]);
  totalPages = signal(0);
  totalElements = signal(0);
  loading = signal(true);

  filters: TransferSearchParams = { page: 0, size: 10 };
  statutFilter = '';
  dateDebut = '';
  dateFin = '';

  readonly statuts: { value: TransferStatus | ''; label: string }[] = [
    { value: '', label: 'Tous les statuts' },
    { value: 'EN_ATTENTE', label: 'En attente' },
    { value: 'PAYE', label: 'Payé' },
    { value: 'ANNULE', label: 'Annulé' },
    { value: 'EXPIRE', label: 'Expiré' },
  ];

  constructor(private transferService: ClientTransferService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const params: TransferSearchParams = {
      ...this.filters,
      statut: (this.statutFilter as TransferStatus) || undefined,
      dateDebut: this.dateDebut || undefined,
      dateFin: this.dateFin || undefined,
    };
    this.transferService.getMyTransfers(params).subscribe((page: any) => {
      this.transfers.set(page.content);
      this.totalPages.set(page.totalPages);
      this.totalElements.set(page.totalElements);
      this.loading.set(false);
    });
  }

  search(): void {
    this.filters.page = 0;
    this.load();
  }

  goToPage(p: number): void {
    this.filters.page = p;
    this.load();
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  statusClass(s: string): string {
    const map: Record<string, string> = {
      PAYE: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
      EN_ATTENTE: 'bg-amber-50 text-amber-700 border border-amber-200',
      ANNULE: 'bg-rose-50 text-rose-700 border border-rose-200',
      EXPIRE: 'bg-slate-100 text-slate-500 border border-slate-200',
    };
    return map[s] ?? '';
  }

  statusLabel(s: string): string {
    const map: Record<string, string> = {
      PAYE: 'Payé',
      EN_ATTENTE: 'En attente',
      ANNULE: 'Annulé',
      EXPIRE: 'Expiré',
    };
    return map[s] ?? s;
  }

  downloadReceipt(id: number): void {
    this.transferService.downloadReceipt(id).subscribe((blob: Blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `recu-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }
}
