import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientProfile, UpdateProfileRequest, ClientActivity, ClientDashboardStats } from '../../pages/client/models/client.model';

@Injectable({ providedIn: 'root' })
export class ClientProfileService {
  private readonly base = '/api/v1/clients';

  constructor(private http: HttpClient) {}

  getMyProfile(): Observable<ClientProfile> {
    return this.http.get<ClientProfile>(`${this.base}/profile`);
  }

  updateMyProfile(req: UpdateProfileRequest): Observable<ClientProfile> {
    return this.http.put<ClientProfile>(`${this.base}/profile`, req);
  }

  getMyActivity(): Observable<ClientActivity[]> {
    return this.http.get<ClientActivity[]>(`${this.base}/profile/activity`);
  }

  getDashboardStats(): Observable<ClientDashboardStats> {
    return this.http.get<ClientDashboardStats>(`${this.base}/dashboard`);
  }

  deleteMyAccount(): Observable<void> {
    return this.http.delete<void>(`${this.base}/profile`);
  }
}
