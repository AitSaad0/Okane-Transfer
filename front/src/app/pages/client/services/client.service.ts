import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { TokenService } from '../../../core/services/token.service';

export interface ClientProfile {
  prenom: string;
  nom: string;
  telephone: string;
  notificationEmail: boolean;
  notificationPush: boolean;
  notificationSms: boolean;
}

export interface ClientProfileUpdate {
  prenom: string;
  nom: string;
  telephone: string;
  notificationEmail: boolean;
  notificationPush: boolean;
  notificationSms: boolean;
}

export interface ActivityItem {
  ipAddress: string;
  action: string;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly API = '/api/v1';
  private readonly AUTH = '/api/auth';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
  ) {}

  private getMe(): Observable<any> {
    return this.http.get<any>(`${this.AUTH}/me`); // ← /api/auth/me
  }

  getProfile(): Observable<ClientProfile> {
    return this.getMe().pipe(
      switchMap((me) =>
        this.http.get<ClientProfile>(`${this.API}/clients/profile`, {
          params: { userId: me.id },
        }),
      ),
    );
  }

  updateProfile(payload: ClientProfileUpdate): Observable<ClientProfile> {
    return this.getMe().pipe(
      switchMap((me) =>
        this.http.put<ClientProfile>(`${this.API}/clients/profile`, payload, {
          params: { userId: me.id },
        }),
      ),
    );
  }

  getActivity(page = 0, size = 20): Observable<any> {
    return this.getMe().pipe(
      switchMap((me) =>
        this.http.get<any>(`${this.API}/clients/profile/activity`, {
          params: { userId: me.id, page, size },
        }),
      ),
    );
  }
}
