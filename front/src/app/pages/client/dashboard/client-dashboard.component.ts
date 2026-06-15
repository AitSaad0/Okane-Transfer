import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TokenService } from '../../../core/services/token.service';
import { ClientTransferService } from '../../../core/services/client-transfer.service';
import { Transfer } from '../transfers/models/client-transfer.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './client-dashboard.component.html',
})
export class ClientDashboardComponent implements OnInit {
  clientName = signal('');
  recentTransfers = signal<Transfer[]>([]);
  totalTransferts = signal(0);
  transfertsEnCours = signal(0);
  loading = signal(true);
  errorTransfers = signal(false);

  constructor(
    private tokenService: TokenService,
    private transferService: ClientTransferService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    // Nom depuis l'API
    this.authService.me().subscribe({
      next: (user: any) => {
        const prenom = user?.prenom ?? user?.firstName ?? '';
        const nom    = user?.nom    ?? user?.lastName  ?? '';
        this.clientName.set(`${prenom} ${nom}`.trim() || user?.email || '');
      }
    });

    // ── Transferts récents ─────────────────────────────────────────────
    this.transferService.getMyTransfers({ page: 0, size: 5 }).subscribe({
      next: (page: any) => {
        const content = page?.content ?? [];
        this.recentTransfers.set(content);
        this.totalTransferts.set(page?.totalElements ?? 0);
        this.transfertsEnCours.set(
          content.filter((t: Transfer) => t.statut === 'EN_ATTENTE').length,
        );
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error('Dashboard transfers error:', err);
        this.errorTransfers.set(true);
        this.loading.set(false);
      },
    });
  }

  statusClass(statut: string): string {
    const map: Record<string, string> = {
      PAYE:       'bg-emerald-50 text-emerald-700 border border-emerald-200',
      EN_ATTENTE: 'bg-amber-50 text-amber-700 border border-amber-200',
      ANNULE:     'bg-rose-50 text-rose-700 border border-rose-200',
      EXPIRE:     'bg-slate-100 text-slate-500 border border-slate-200',
    };
    return map[statut] ?? 'bg-slate-100 text-slate-500';
  }

  statusLabel(statut: string): string {
    const map: Record<string, string> = {
      PAYE:       'Payé',
      EN_ATTENTE: 'En attente',
      ANNULE:     'Annulé',
      EXPIRE:     'Expiré',
    };
    return map[statut] ?? statut;
  }
}
