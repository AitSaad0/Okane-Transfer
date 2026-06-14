import { Component, inject, Input, OnChanges, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeeGridService, FeeSimulateResult } from '../../../core/services/fee-grid.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-fee-simulator',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './fee-simulator.component.html',
  styleUrl: './fee-simulator.component.css'
})
export class FeeSimulatorComponent implements OnChanges {
  @Input() corridorId: number | null = null;
  @Input() amount: number | null = null;

  private svc = inject(FeeGridService);

  result = signal<FeeSimulateResult | null>(null);
  loading = signal(false);
  error = signal('');

  ngOnChanges(changes: SimpleChanges): void {
    if (this.corridorId && this.amount && this.amount > 0) {
      this.simulate();
    } else {
      this.result.set(null);
    }
  }

  simulate(): void {
    if (!this.corridorId || !this.amount) return;
    this.loading.set(true);
    this.error.set('');
    this.svc.simulate({ corridorId: this.corridorId, amount: this.amount }).subscribe({
      next: r => { this.result.set(r); this.loading.set(false); },
      error: () => { this.error.set('Simulation impossible'); this.loading.set(false); }
    });
  }
}
