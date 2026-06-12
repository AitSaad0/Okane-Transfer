import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CurrencyService, Currency, PagedResponse } from '../../../core/services/currency.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-currencies',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './currencies.component.html',
  styleUrl: './currencies.component.css'
})
export class CurrenciesComponent implements OnInit {
  private svc = inject(CurrencyService);

  data = signal<PagedResponse<Currency>>({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  loading = signal(false);
  error = signal('');
  page = signal(0);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getAll(this.page(), 10).subscribe({
      next: r => { this.data.set(r); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement'); this.loading.set(false); }
    });
  }

  toggle(c: Currency): void {
    this.svc.toggleStatus(c.id, !c.active).subscribe({ next: () => this.load() });
  }

  prevPage(): void { if (this.page() > 0) { this.page.update(p => p - 1); this.load(); } }
  nextPage(): void { if (this.page() < this.data().totalPages - 1) { this.page.update(p => p + 1); this.load(); } }
}
