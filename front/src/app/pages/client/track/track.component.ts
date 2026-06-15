import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientTransferService } from '../../../core/services/client-transfer.service';
import { Transfer } from '../transfers/models/client-transfer.model';


@Component({
  selector: 'app-track',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './track.component.html',
})
export class TrackComponent {
  reference = '';
  transfer = signal<Transfer | null>(null);
  loading = signal(false);
  error = signal('');
  searched = signal(false);

  constructor(private transferService: ClientTransferService) {}

  search(): void {
    if (!this.reference.trim()) return;
    this.loading.set(true);
    this.error.set('');
    this.transfer.set(null);
    this.searched.set(false);

    this.transferService.trackTransfer(this.reference.trim()).subscribe({
      next: t => { this.transfer.set(t); this.loading.set(false); this.searched.set(true); },
      error: () => { this.error.set('Aucun transfert trouvé pour cette référence.'); this.loading.set(false); this.searched.set(true); }
    });
  }

  statusClass(s: string): string {
    return { PAYE: 'bg-emerald-50 text-emerald-700 border border-emerald-200', EN_ATTENTE: 'bg-amber-50 text-amber-700 border border-amber-200', ANNULE: 'bg-rose-50 text-rose-700 border border-rose-200', EXPIRE: 'bg-slate-100 text-slate-500 border border-slate-200' }[s] ?? '';
  }

  statusLabel(s: string): string {
    return { PAYE: 'Payé ✓', EN_ATTENTE: 'En attente…', ANNULE: 'Annulé', EXPIRE: 'Expiré' }[s] ?? s;
  }

  get steps(): { label: string; done: boolean; active: boolean }[] {
    const t = this.transfer();
    if (!t) return [];
    const statut = t.statut;
    return [
      { label: 'Transfert créé', done: true, active: false },
      { label: 'Traitement en cours', done: statut === 'PAYE' || statut === 'ANNULE', active: statut === 'EN_ATTENTE' },
      { label: 'Disponible au retrait', done: statut === 'PAYE', active: false },
    ];
  }
}
