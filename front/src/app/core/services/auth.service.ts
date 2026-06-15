import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { TokenService } from './token.service';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}
export interface LoginRequest {
  email: string;
  password: string;
}
export interface RegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  telephone: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly BASE = '/api/auth';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private router: Router
  ) {}

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE}/login`, payload).pipe(
      tap(res => {
        this.tokenService.setAccessToken(res.accessToken);
        this.tokenService.setRefreshToken(res.refreshToken);
        this.redirectByRole();
      })
    );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE}/register`, payload).pipe(
      tap(res => {
        this.tokenService.setAccessToken(res.accessToken);
        this.tokenService.setRefreshToken(res.refreshToken);
        this.redirectByRole();
      })
    );
  }

  redirectByRole(): void {
    const role = this.tokenService.getRole();
    switch (role) {
      case 'ADMIN':   this.router.navigate(['/admin']);            break;
      case 'MANAGER': this.router.navigate(['/manager']);          break;
      case 'AGENT':   this.router.navigate(['/agent']);            break;
      case 'CLIENT':  this.router.navigate(['/client']); break;
      default:        this.router.navigate(['/unauthorized']);      break;
    }
  }

  logout(): void {
    this.tokenService.clear();
    this.router.navigate(['/login']);
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.BASE}/password/forgot`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.BASE}/password/reset`, { token, newPassword });
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.tokenService.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.BASE}/refresh`, { refreshToken }).pipe(
      tap(res => {
        this.tokenService.setAccessToken(res.accessToken);
        this.tokenService.setRefreshToken(res.refreshToken);
      })
    );
  }

  me(): Observable<any> {
    return this.http.get(`${this.BASE}/me`);
  }

  isLoggedIn(): boolean {
    return this.tokenService.isLoggedIn();
  }

  getRole(): string | null {
    return this.tokenService.getRole();
  }
}
