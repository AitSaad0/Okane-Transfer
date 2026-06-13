// notifications.component.ts
import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from './services/notification.service';
import {
  NotificationResponseDto,
  TypeNotification,
} from './models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsComponent implements OnInit {
  all: NotificationResponseDto[] = [];
  filtered: NotificationResponseDto[] = [];
  loading = true;
  error = '';

  selectedType: TypeNotification | 'ALL' = 'ALL';

  readonly types: Array<TypeNotification | 'ALL'> = [
    'ALL',
    'TRANSFERT_CREE',
    'TRANSFERT_PAYE',
    'TRANSFERT_ANNULE',
    'TRANSFERT_EXPIRE',
    'RECU_EXPEDITEUR',
    'CONFIRMATION_RETRAIT',
    'SYSTEME',
    'MAINTENANCE',
    'ALERTE',
  ];

  readonly typeLabels: Record<TypeNotification | 'ALL', string> = {
    ALL: 'Toutes',
    TRANSFERT_CREE: 'Transfert créé',
    TRANSFERT_PAYE: 'Transfert payé',
    TRANSFERT_ANNULE: 'Transfert annulé',
    TRANSFERT_EXPIRE: 'Transfert expiré',
    RECU_EXPEDITEUR: 'Reçu expéditeur',
    CONFIRMATION_RETRAIT: 'Confirmation retrait',
    SYSTEME: 'Système',
    MAINTENANCE: 'Maintenance',
    ALERTE: 'Alerte',
  };

  constructor(
    private notifService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.notifService.getAll().subscribe({
      next: (data) => {
        this.all = data.sort(
          (a, b) =>
            new Date(b.dateEnvoi).getTime() - new Date(a.dateEnvoi).getTime()
        );
        this.applyFilter();
        this.loading = false;
        this.cdr.markForCheck(); // ← triggers re-render after async data arrives
      },
      error: () => {
        this.error = 'Impossible de charger les notifications.';
        this.loading = false;
        this.cdr.markForCheck(); // ← triggers re-render on error too
      },
    });
  }

  applyFilter(): void {
    this.filtered =
      this.selectedType === 'ALL'
        ? [...this.all]
        : this.all.filter((n) => n.type === this.selectedType);
  }

  onTypeChange(type: TypeNotification | 'ALL'): void {
    this.selectedType = type;
    this.applyFilter();
    this.cdr.markForCheck(); // ← re-render when filter changes
  }

  markRead(notif: NotificationResponseDto): void {
    if (notif.lu) return;
    this.notifService.markAsRead(notif.id).subscribe({
      next: () => {
        notif.lu = true;
        this.cdr.markForCheck(); // ← re-render after optimistic update
      },
    });
  }

  get unreadCount(): number {
    return this.all.filter((n) => !n.lu).length;
  }

  typeIcon(type: TypeNotification): string {
    const icons: Record<TypeNotification, string> = {
      TRANSFERT_CREE: '💸',
      TRANSFERT_PAYE: '✅',
      TRANSFERT_ANNULE: '❌',
      TRANSFERT_EXPIRE: '⏰',
      RECU_EXPEDITEUR: '🧾',
      CONFIRMATION_RETRAIT: '🏧',
      SYSTEME: '🔧',
      MAINTENANCE: '🛠️',
      ALERTE: '🚨',
    };
    return icons[type] ?? '🔔';
  }
}
