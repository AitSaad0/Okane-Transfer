import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FeeGridService, FeeGrid } from '../../../core/services/fee-grid.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-fee-grids',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './fee-grids.component.html',
  styleUrl: './fee-grids.component.css'
})
export class FeeGridsComponent implements OnInit {
  private svc = inject(FeeGridService);

  grids = signal<FeeGrid[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: r => { this.grids.set(r); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement'); this.loading.set(false); }
    });
  }

  delete(id: number): void {
    if (!confirm('Supprimer cette grille ?')) return;
    this.svc.delete(id).subscribe({
      next: () => { this.success.set('Grille supprimée'); this.load(); setTimeout(() => this.success.set(''), 3000); },
      error: () => this.error.set('Suppression impossible — grille utilisée')
    });
  }

  exportFile(format: 'csv' | 'pdf'): void {
    this.svc.export(format).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `fee-grids.${format}`; a.click();
      URL.revokeObjectURL(url);
    });
  }
}
