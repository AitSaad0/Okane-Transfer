import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';
import { HttpClient } from '@angular/common/http';
import { catchError, switchMap, throwError } from 'rxjs';

const PUBLIC_URLS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/forgot-password',
  '/api/auth/reset-password',
  '/api/auth/refresh',
];

let isRefreshing = false;

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const isPublic = PUBLIC_URLS.some(url => req.url.includes(url));
  if (isPublic) return next(req);

  const tokenService = inject(TokenService);
  const http = inject(HttpClient);

  const token = tokenService.getAccessToken();
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isRefreshing) {
        return throwError(() => error);
      }

      isRefreshing = true;
      const refreshToken = tokenService.getRefreshToken();

      if (!refreshToken) {
        isRefreshing = false;
        tokenService.clear();
        window.location.href = '/login';
        return throwError(() => error);
      }

      return http.post<{ accessToken: string }>(
        '/api/auth/refresh',
        { refreshToken }
      ).pipe(
        switchMap(res => {
          isRefreshing = false;
          tokenService.setAccessToken(res.accessToken);

          // Retry original request with new token
          const retried = req.clone({
            setHeaders: { Authorization: `Bearer ${res.accessToken}` }
          });
          return next(retried);
        }),
        catchError(refreshErr => {
          isRefreshing = false;
          tokenService.clear();
          window.location.href = '/login';
          return throwError(() => refreshErr);
        })
      );
    })
  );
};
