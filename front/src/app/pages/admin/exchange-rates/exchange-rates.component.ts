import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ExchangeRateService, ExchangeRate } from '../../../core/services/exchange-rate.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-exchange-rates',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './exchange-rates.component.html',
  styleUrl: './exchange-rates.component.css',
})
export class ExchangeRatesComponent implements OnInit {
  private svc = inject(ExchangeRateService);
  private fb = inject(FormBuilder);

  rates = signal<ExchangeRate[]>([]);
  loading = signal(false);
  syncing = signal(false);
  error = signal('');
  success = signal('');
  editingRate = signal<ExchangeRate | null>(null);

  editForm = this.fb.group({
    fromCurrency: [''],
    toCurrency: [''],
    rate: [0, [Validators.required, Validators.min(0.0001)]],
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: (r) => {
        this.rates.set(r);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erreur chargement');
        this.loading.set(false);
      },
    });
  }

  startEdit(r: ExchangeRate): void {
    this.editingRate.set(r);
    this.editForm.patchValue({
      fromCurrency: r.fromCurrency,
      toCurrency: r.toCurrency,
      rate: r.rate,
    });
  }

  cancelEdit(): void {
    this.editingRate.set(null);
  }

  saveEdit(): void {
    const rate = this.editingRate();
    if (!rate || this.editForm.invalid) return;

    this.svc.updateManual(rate.corridorId, this.editForm.value.rate!).subscribe({
      next: () => {
        this.success.set('Taux mis à jour');
        this.editingRate.set(null);
        this.load();
        setTimeout(() => this.success.set(''), 3000);
      },
      error: () => this.error.set('Erreur mise à jour'),
    });
  }

  sync(): void {
    this.syncing.set(true);
    this.svc.syncFromApi().subscribe({
      next: (r) => {
        this.success.set(r);
        this.syncing.set(false);
        this.load();
        setTimeout(() => this.success.set(''), 3000);
      },
      error: (err) => {
        console.error('Erreur sync:', err);
        this.error.set('Erreur synchronisation');
        this.syncing.set(false);
      },
    });
  }
}
