import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BroadcastNotificationRequest,
  BroadcastNotificationResponse
} from '../models/broadcast-notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl = '/api/v1/admin/notifications';

  constructor(private http: HttpClient) {}

  broadcast(request: BroadcastNotificationRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/broadcast`, request);
  }

  getAllBroadcasts(): Observable<BroadcastNotificationResponse[]> {
    return this.http.get<BroadcastNotificationResponse[]>(`${this.baseUrl}/broadcast`);
  }
}
