import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ExchangeRateService, ExchangeRate } from '../../../core/services/exchange-rate.service';
import { TranslateModule } from '@ngx-translate/core';
import { JavaDatePipe } from '../../../core/pipes/java-date.pipe';

@Component({
  selector: 'app-exchange-rates-history',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, JavaDatePipe],
  templateUrl: './exchange-rates-history.component.html',
  styleUrl: './exchange-rates-history.component.css'
})
export class ExchangeRatesHistoryComponent implements OnInit {
  private svc = inject(ExchangeRateService);

  history = signal<ExchangeRate[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit(): void {
    this.loading.set(true);
    this.svc.getHistory(1).subscribe({
      next: r => { this.history.set(r); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement'); this.loading.set(false); }
    });
  }
}