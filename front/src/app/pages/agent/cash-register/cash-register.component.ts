import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CashRegisterService } from '../../../core/services/cash-register.service';
import { Caisse, OperationCaisse } from '../models/caisse.model';

@Component({
  selector: 'app-cash-register',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cash-register.component.html',
  styleUrls: ['./cash-register.component.css'],
})
export class CashRegisterComponent implements OnInit {
  caisse = signal<Caisse | null>(null);
  operations = signal<OperationCaisse[]>([]);
  loading = signal(true);

  constructor(private cashService: CashRegisterService) {}

  ngOnInit(): void {
    this.cashService.getMyCaisse().subscribe((c: Caisse) => {
      this.caisse.set(c);
      this.loading.set(false);
    });
    this.cashService.getOperations().subscribe((ops: OperationCaisse[]) =>
      this.operations.set(ops)
    );
  }

  opTypeLabel(type: string): string {
    const map: Record<string, string> = {
      TRANSFERT_PAYE: 'Paiement transfert',
      TRANSFERT_ENVOYE: 'Envoi transfert',
      AJUSTEMENT: 'Ajustement',
    };
    return map[type] ?? type;
  }

  opTypeClass(type: string): string {
    const map: Record<string, string> = {
      TRANSFERT_PAYE: 'text-emerald-600',
      TRANSFERT_ENVOYE: 'text-rose-600',
      AJUSTEMENT: 'text-amber-600',
    };
    return map[type] ?? 'text-slate-600';
  }
}
