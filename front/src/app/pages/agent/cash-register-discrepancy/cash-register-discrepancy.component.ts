import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CashRegisterService } from '../../../core/services/cash-register.service';
import { Caisse } from '../models/caisse.model';

@Component({
  selector: 'app-cash-register-discrepancy',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cash-register-discrepancy.component.html',
  styleUrls: ['./cash-register-discrepancy.component.css'],
})
export class CashRegisterDiscrepancyComponent implements OnInit {
  caisse = signal<Caisse | null>(null);
  soldeReel: number | null = null;
  motif = '';
  loading = signal(false);
  error = signal('');

  constructor(private cashService: CashRegisterService, private router: Router) {}

  ngOnInit(): void {
    this.cashService.getMyCaisse().subscribe((c: Caisse) => this.caisse.set(c));
  }

  get ecart(): number {
    const c = this.caisse();
    if (!c || this.soldeReel === null) return 0;
    return this.soldeReel - c.soldeCourant;
  }

  submit(): void {
    if (this.soldeReel === null || !this.motif.trim()) {
      this.error.set('Tous les champs sont obligatoires.');
      return;
    }
    this.loading.set(true);
    this.cashService
      .declareDiscrepancy({
        soldeReel: this.soldeReel,
        soldeSysteme: this.caisse()!.soldeCourant,
        motif: this.motif,
      })
      .subscribe({
        next: () => this.router.navigate(['/agent/cash-register']),
        error: () => {
          this.loading.set(false);
          this.error.set('Erreur lors de la déclaration.');
        },
      });
  }
}
