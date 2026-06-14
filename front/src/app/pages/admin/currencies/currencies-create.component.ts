import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormArray } from '@angular/forms';
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
  showCountryForm = signal(false);

  form = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
    name: ['', Validators.required],
    pays: this.fb.array([], Validators.minLength(1))
  });

  countryForm = this.fb.group({
    nom:     ['', Validators.required],
    codeIso: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(2)]]
  });

  get pays(): FormArray { return this.form.get('pays') as FormArray; }

  get canSubmit(): boolean {
    return this.form.get('code')!.valid &&
      this.form.get('name')!.valid &&
      this.pays.length >= 1 &&
      !this.loading();
  }

  openCountryForm(): void {
    this.countryForm.reset();
    this.showCountryForm.set(true);
  }

  cancelCountryForm(): void {
    this.showCountryForm.set(false);
    this.countryForm.reset();
  }

  addCountry(): void {
    if (this.countryForm.invalid) return;
    const v = this.countryForm.value;
    this.pays.push(this.fb.group({
      nom:     [v.nom],
      codeIso: [v.codeIso!.toUpperCase()]
    }));
    this.showCountryForm.set(false);
    this.countryForm.reset();
  }

  removeCountry(i: number): void {
    this.pays.removeAt(i);
  }

  submit(): void {
    if (!this.canSubmit) return;
    this.loading.set(true);
    const v = this.form.value;
    this.svc.create({
      code:      v.code!.toUpperCase(),
      symbol:    '',
      name:      v.name!,
      countries: this.pays.controls.map(c => c.get('nom')!.value)
    }).subscribe({
      next: () => this.router.navigate(['/admin/currencies']),
      error: () => { this.error.set('Erreur lors de la création'); this.loading.set(false); }
    });
  }
}
