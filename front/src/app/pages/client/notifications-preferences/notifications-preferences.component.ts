import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface NotificationPreferenceDto {
  emailActive: boolean;
  pushActive: boolean;
  smsActive: boolean;
}

@Component({
  selector: 'app-notification-preferences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notifications-preferences.component.html',
  styleUrls: ['./notifications-preferences.component.css']
})
export class NotificationPreferencesComponent implements OnInit {

  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);

  preferences: NotificationPreferenceDto = {
    emailActive: true,
    pushActive: false,
    smsActive: true
  };

  loading = false;
  saving = false;
  successMessage = '';
  errorMessage = '';

  ngOnInit(): void {
    this.loadPreferences();
  }

  loadPreferences(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http
      .get<any>('/api/v1/notifications/preferences')
      .subscribe({
        next: (data) => {

          this.preferences = {
            emailActive: data?.emailActive ?? false,
            pushActive: data?.pushActive ?? false,
            smsActive: data?.smsActive ?? false
          };

          console.log('Loaded preferences:', this.preferences);

          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);

          this.loading = false;
          this.errorMessage = 'Failed to load preferences';

          this.cdr.detectChanges();
        }
      });
  }

  savePreferences(): void {
    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload: NotificationPreferenceDto = {
      emailActive: this.preferences.emailActive ?? false,
      pushActive: this.preferences.pushActive ?? false,
      smsActive: this.preferences.smsActive ?? false
    };

    console.log('Sending preferences:', payload);

    this.http
      .put<void>(
        '/api/v1/notifications/preferences',
        payload
      )
      .subscribe({
        next: () => {
          this.saving = false;
          this.successMessage =
            'Preferences updated successfully';

          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);

          this.saving = false;
          this.errorMessage =
            'Failed to update preferences';

          this.cdr.detectChanges();
        }
      });
  }
}
