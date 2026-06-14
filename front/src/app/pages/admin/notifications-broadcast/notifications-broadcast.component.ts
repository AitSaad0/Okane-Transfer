import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NotificationService } from './service/notification.service';
import {
  TypeNotification,
  CanalNotification,
  BroadcastNotificationResponse
} from './models/broadcast-notification.model';

@Component({
  selector: 'app-broadcast-notification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './notifications-broadcast.component.html',
  styleUrls: ['./notifications-broadcast.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BroadcastNotificationComponent implements OnInit {
  form: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';

  history: BroadcastNotificationResponse[] = [];
  loadingHistory = false;

  typeOptions = Object.values(TypeNotification);
  canalOptions = Object.values(CanalNotification);

  constructor(
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      type: ['', Validators.required],
      canal: ['', Validators.required],
      contenu: ['', [Validators.required, Validators.maxLength(1000)]]
    });
  }

  ngOnInit(): void {
    this.loadHistory();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.cdr.markForCheck();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.notificationService.broadcast(this.form.value).subscribe({
      next: () => {
        this.successMessage = 'Notification diffusée avec succès.';
        this.form.reset();
        this.loadHistory();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Erreur lors de l\'envoi de la notification.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadHistory(): void {
    this.loadingHistory = true;
    this.cdr.markForCheck();

    this.notificationService.getAllBroadcasts().subscribe({
      next: (data) => {

        this.history = data.map((item: any) => ({
          ...item,
          dateEnvoi: this.convertDate(item.dateEnvoi)
        }));

        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cdr.markForCheck();
      }
    });
  }

  private convertDate(value: any): Date | null {

    if (!value) {
      return null;
    }

    // LocalDateTime serialized as array
    if (Array.isArray(value)) {
      return new Date(
        value[0],
        value[1] - 1,
        value[2],
        value[3] || 0,
        value[4] || 0,
        value[5] || 0
      );
    }

    // "2026,6,14,1,27,35"
    if (typeof value === 'string' && value.includes(',')) {
      const p = value.split(',').map(Number);

      return new Date(
        p[0],
        p[1] - 1,
        p[2],
        p[3] || 0,
        p[4] || 0,
        p[5] || 0
      );
    }

    return new Date(value);
  }
}
