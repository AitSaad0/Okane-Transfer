import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CashRegisterService } from '../../../core/services/cash-register.service';
import { Caisse } from '../models/caisse.model';

@Component({
  selector: 'app-cash-register-close',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cash-register-close.component.html',
  styleUrls: ['./cash-register-close.component.css'],
})
export class CashRegisterCloseComponent implements OnInit {
  caisse = signal<Caisse | null>(null);
  soldeDeclare: number | null = null;
  commentaire = '';
  loading = signal(false);
  error = signal('');

  constructor(private cashService: CashRegisterService, private router: Router) {}

  ngOnInit(): void {
    this.cashService.getMyCaisse().subscribe((c: Caisse) => this.caisse.set(c));
  }

  get ecart(): number {
    const c = this.caisse();
    if (!c || this.soldeDeclare === null) return 0;
    return this.soldeDeclare - c.soldeCourant;
  }

  submit(): void {
    if (this.soldeDeclare === null) {
      this.error.set('Veuillez saisir le solde réel.');
      return;
    }
    this.loading.set(true);
    this.cashService
      .closeCaisse({ soldeDeclareFermeture: this.soldeDeclare, commentaire: this.commentaire })
      .subscribe({
        next: () => this.router.navigate(['/agent/cash-register']),
        error: () => {
          this.loading.set(false);
          this.error.set('Erreur lors de la clôture.');
        },
      });
  }
}
