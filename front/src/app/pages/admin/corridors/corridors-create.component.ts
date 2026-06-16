import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CorridorService } from '../../../core/services/corridor.service';
import { CurrencyService, Currency } from '../../../core/services/currency.service';
import { CountryService, Country } from '../../../core/services/country.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-corridors-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './corridors-create.component.html',
  styleUrl: './corridors-create.component.css',
})
export class CorridorsCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private svc = inject(CorridorService);
  private currSvc = inject(CurrencyService);
  private countrySvc = inject(CountryService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');
  currencies = signal<Currency[]>([]);

  countries = signal<Country[]>([]);

  form = this.fb.group({
    sourceCountryCode: ['', Validators.required],
    destinationCountryCode: ['', Validators.required],
    sourceCurrencyCode: ['', Validators.required],
    destinationCurrencyCode: ['', Validators.required],
  });

  ngOnInit(): void {
    this.currSvc.getAll(0, 100, true).subscribe({
      next: (r) => this.currencies.set(r.content),
    });
    this.countrySvc.getAll().subscribe({
      next: (countries) => this.countries.set(countries),
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);

    const v = this.form.value;

    const dto = {
      paysSourceCode: v.sourceCountryCode!,
      paysDestinationCode: v.destinationCountryCode!,
      deviseSourceCode: v.sourceCurrencyCode!,
      deviseDestinationCode: v.destinationCurrencyCode!,
    };

    this.svc.create(dto).subscribe({
      next: () => this.router.navigate(['/admin/corridors']),
      error: (err) => {
        console.error(err);
        this.error.set('Erreur lors de la création');
        this.loading.set(false);
      },
    });
  }
}
