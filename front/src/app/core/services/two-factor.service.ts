import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OtpResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class TwoFactorService {

  private readonly BASE = '/api/auth/2fa';

  constructor(private http: HttpClient) {}

  sendOtp(email: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.BASE}/send`, { email });
  }

  verifyOtp(email: string, code: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.BASE}/verify`, { email, code });
  }
}
