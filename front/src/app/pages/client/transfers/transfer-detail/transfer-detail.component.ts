import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ClientTransferService } from '../../../../core/services/client-transfer.service';
import { Transfer } from '../models/client-transfer.model';

@Component({
  selector: 'app-client-transfer-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './transfer-detail.component.html',
})
export class ClientTransferDetailComponent implements OnInit {
  transfer = signal<Transfer | null>(null);
  loading = signal(true);
  error = signal('');

  constructor(
    private route: ActivatedRoute,
    private transferService: ClientTransferService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.transferService.getMyTransferById(id).subscribe({
      next: t => { this.transfer.set(t); this.loading.set(false); },
      error: () => { this.error.set('Transfert introuvable.'); this.loading.set(false); }
    });
  }

  downloadReceipt(): void {
    const t = this.transfer();
    if (!t) return;
    this.transferService.downloadReceipt(t.id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = `recu-${t.codeRetrait}.pdf`; a.click();
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
