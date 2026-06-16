import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ClientTransferService } from '../../../core/services/client-transfer.service';
import { Transfer, TransferStatus, TransferSearchParams } from '../../client/transfers/models/client-transfer.model';

@Component({
  selector: 'app-admin-transfers',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-transfers.component.html',
})
export class AdminTransfersComponent implements OnInit {
  transfers = signal<Transfer[]>([]);
  totalPages = signal(0);
  totalElements = signal(0);
  loading = signal(true);
  cancellingId = signal<number | null>(null);
  showCancelModal = signal(false);
  selectedTransfer = signal<Transfer | null>(null);
  cancelMotif = '';

  filters: TransferSearchParams = { page: 0, size: 10 };
  referenceFilter = '';
  statutFilter = '';
  dateDebut = '';
  dateFin = '';

  readonly statuts = [
    { value: '', label: 'Tous les statuts' },
    { value: 'EN_ATTENTE', label: 'En attente' },
    { value: 'PAYE', label: 'Payé' },
    { value: 'ANNULE', label: 'Annulé' },
    { value: 'EXPIRE', label: 'Expiré' },
  ];

  constructor(private transferService: ClientTransferService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    const params: TransferSearchParams = {
      ...this.filters,
      reference: this.referenceFilter || undefined,
      statut: this.statutFilter as TransferStatus || undefined,
      dateDebut: this.dateDebut || undefined,
      dateFin: this.dateFin || undefined,
    };
    this.transferService.getAllTransfers(params).subscribe(page => {
      this.transfers.set(page.content);
      this.totalPages.set(page.totalPages);
      this.totalElements.set(page.totalElements);
      this.loading.set(false);
    });
  }

  search(): void { this.filters.page = 0; this.load(); }

  goToPage(p: number): void { this.filters.page = p; this.load(); }

  get pages(): number[] { return Array.from({ length: this.totalPages() }, (_, i) => i); }

  openCancelModal(t: Transfer): void {
    this.selectedTransfer.set(t);
    this.cancelMotif = '';
    this.showCancelModal.set(true);
  }

  confirmCancel(): void {
    const t = this.selectedTransfer();
    if (!t || !this.cancelMotif.trim()) return;
    this.cancellingId.set(t.id);
    this.transferService.forceCancel(t.id, this.cancelMotif).subscribe({
      next: () => { this.showCancelModal.set(false); this.cancellingId.set(null); this.load(); },
      error: () => this.cancellingId.set(null)
    });
  }

  downloadReceipt(id: number): void {
    this.transferService.downloadReceipt(id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `recu-${id}.pdf`; a.click();
      URL.revokeObjectURL(url);
    });
  }

  statusClass(s: string): string {
    return { PAYE: 'bg-emerald-50 text-emerald-700 border border-emerald-200', EN_ATTENTE: 'bg-amber-50 text-amber-700 border border-amber-200', ANNULE: 'bg-rose-50 text-rose-700 border border-rose-200', EXPIRE: 'bg-slate-100 text-slate-500 border border-slate-200' }[s] ?? '';
  }

  statusLabel(s: string): string {
    return { PAYE: 'Payé', EN_ATTENTE: 'En attente', ANNULE: 'Annulé', EXPIRE: 'Expiré' }[s] ?? s;
  }
}
