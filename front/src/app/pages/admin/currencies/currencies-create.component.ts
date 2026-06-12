import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CurrencyService } from '../../../core/services/currency.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-currencies-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './currencies-create.component.html',
  styleUrl: './currencies-create.component.css'
})
export class CurrenciesCreateComponent {
  private fb = inject(FormBuilder);
  private svc = inject(CurrencyService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');

  form = this.fb.group({
    code:      ['', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    symbol:    ['', Validators.required],
    name:      ['', Validators.required],
    countries: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const v = this.form.value;
    this.svc.create({
      code: v.code!.toUpperCase(),
      symbol: v.symbol!,
      name: v.name!,
      countries: v.countries!.split(',').map(c => c.trim())
    }).subscribe({
      next: () => this.router.navigate(['/admin/currencies']),
      error: () => { this.error.set('Erreur lors de la création'); this.loading.set(false); }
    });
  }
}
