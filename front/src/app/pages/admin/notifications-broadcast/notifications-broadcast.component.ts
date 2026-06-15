import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from './service/notification.service';
import {
  TypeNotification,
  CanalNotification,
  BroadcastNotificationResponse
} from './models/broadcast-notification.model';

@Component({
  selector: 'app-broadcast-notification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
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
        this.history = data;
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cdr.markForCheck();
      }
    });
  }
  toDate(value: any): Date | null {
    if (!value) return null;
    if (Array.isArray(value)) {
      // [year, month, day, hour, min, sec] — month est 1-based côté Java
      const [year, month, day, hour = 0, min = 0, sec = 0] = value;
      return new Date(year, month - 1, day, hour, min, sec);
    }
    return new Date(value); // string ISO ou timestamp
  }
}
