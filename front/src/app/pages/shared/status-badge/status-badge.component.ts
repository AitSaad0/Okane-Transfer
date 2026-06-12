import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.css'],
})
export class StatusBadgeComponent {
  @Input() status!: string;

  get config(): { label: string; classes: string } {
    const map: Record<string, { label: string; classes: string }> = {
      // user active/suspended
      true: { label: 'Actif', classes: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
      false: { label: 'Suspendu', classes: 'bg-rose-50    text-rose-700    border-rose-200' },
      ACTIVE: { label: 'Actif', classes: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
      SUSPENDUE: { label: 'Suspendue', classes: 'bg-rose-50    text-rose-700    border-rose-200' },
      // roles
      ROLE_ADMIN: { label: 'Admin', classes: 'bg-indigo-50  text-indigo-700  border-indigo-200' },
      ROLE_MANAGER: {
        label: 'Manager',
        classes: 'bg-violet-50  text-violet-700  border-violet-200',
      },
      ROLE_AGENT: { label: 'Agent', classes: 'bg-sky-50     text-sky-700     border-sky-200' },
      ROLE_CLIENT: { label: 'Client', classes: 'bg-slate-50   text-slate-600   border-slate-200' },
    };
    return (
      map[this.status] ?? {
        label: this.status,
        classes: 'bg-slate-50 text-slate-600 border-slate-200',
      }
    );
  }
}
