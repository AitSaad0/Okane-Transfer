// notification.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationResponseDto } from '../models/notification.model';
import { NotificationPreferenceDto } from '../models/notification-preference.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly base = '/api/v1';

  constructor(private http: HttpClient) {}

  getAll(): Observable<NotificationResponseDto[]> {
    return this.http.get<NotificationResponseDto[]>(
      `${this.base}/notifications`
    );
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(
      `${this.base}/notifications/${id}/read`,
      {}
    );
  }

  // path updated: /clients/notifications/prefs → /notifications/preferences
  updatePreferences(dto: NotificationPreferenceDto): Observable<void> {
    return this.http.put<void>(
      `${this.base}/notifications/preferences`,
      dto
    );
  }

  // new: broadcast endpoint (admin only)
  broadcast(payload: {
    type: string;
    canal: string;
    contenu: string;
  }): Observable<void> {
    return this.http.post<void>(
      `${this.base}/admin/notifications/broadcast`,
      payload
    );
  }
}
