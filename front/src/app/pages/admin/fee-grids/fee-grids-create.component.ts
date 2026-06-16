import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormArray, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { FeeGridService, FeeGridCreateDTO } from '../../../core/services/fee-grid.service';
import { CorridorService, Corridor } from '../../../core/services/corridor.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-fee-grids-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
  templateUrl: './fee-grids-create.component.html',
  styleUrl: './fee-grids-create.component.css',
})
export class FeeGridsCreateComponent implements OnInit {
  private fb = inject(FormBuilder);
  private svc = inject(FeeGridService);
  private corridorSvc = inject(CorridorService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');
  corridors = signal<Corridor[]>([]);

  form = this.fb.group({
    corridorId: [null as number | null, Validators.required],
    montantMin: [0, Validators.required],
    montantMax: [0, Validators.required],
    fraisFixe: [0, Validators.required],
    pourcentageFrais: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    partAgence: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    slices: this.fb.array([this.createSliceGroup()]),
  });

  get slices(): FormArray<FormGroup> {
    return this.form.get('slices') as FormArray<FormGroup>;
  }

  createSliceGroup(): FormGroup {
    return this.fb.group({
      minAmount: [0, Validators.required],
      maxAmount: [0, Validators.required],
      fixedFee: [0, Validators.required],
      agencyShare: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
      centralShare: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    });
  }

  addSlice(): void {
    this.slices.push(this.createSliceGroup());
  }

  removeSlice(index: number): void {
    if (this.slices.length > 1) {
      this.slices.removeAt(index);
    }
  }

  ngOnInit(): void {
    this.corridorSvc.getActive().subscribe({ next: (r) => this.corridors.set(r) });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.svc.create(this.form.value as FeeGridCreateDTO).subscribe({
      next: () => this.router.navigate(['/admin/fee-grids']),
      error: (err) => {
        this.error.set(err.error?.message || 'Erreur lors de la création');
        this.loading.set(false);
      },
    });
  }
}
