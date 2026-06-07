import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CorridorService, Corridor } from '../../../core/services/corridor.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-corridors',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './corridors.component.html',
  styleUrl: './corridors.component.css'
})
export class CorridorsComponent implements OnInit {
  private svc = inject(CorridorService);

  corridors = signal<Corridor[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: r => { this.corridors.set(r); this.loading.set(false); },
      error: () => { this.error.set('Erreur chargement'); this.loading.set(false); }
    });
  }

  toggle(c: Corridor): void {
    this.svc.toggleStatus(c.id, !c.active).subscribe({ next: () => this.load() });
  }
}
