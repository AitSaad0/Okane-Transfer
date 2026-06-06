import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenService {

  private readonly ACCESS_KEY  = 'access_token';
  private readonly REFRESH_KEY = 'refresh_token';
  private readonly LANG_KEY    = 'lang';

  // ── Access Token ──────────────────────────────────────────────────────
  setAccessToken(token: string): void {
    localStorage.setItem(this.ACCESS_KEY, token);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_KEY);
  }

  // ── Refresh Token ─────────────────────────────────────────────────────
  setRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_KEY, token);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  // ── Clear ─────────────────────────────────────────────────────────────
  clear(): void {
    localStorage.removeItem(this.ACCESS_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
  }

  // ── Language ──────────────────────────────────────────────────────────
  setLang(lang: string): void {
    localStorage.setItem(this.LANG_KEY, lang);
  }

  getLang(): string {
    return localStorage.getItem(this.LANG_KEY) ?? 'fr';
  }

  // ── Helpers ───────────────────────────────────────────────────────────
  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  decodeToken(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    const token = this.getAccessToken();
    if (!token) return null;
    const payload = this.decodeToken(token);
    return payload?.role ?? null;
  }
}
