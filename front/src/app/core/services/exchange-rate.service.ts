import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ExchangeRate {
  id: number;
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  source: 'MANUAL' | 'API';
  updatedAt: string;
}

export interface ExchangeRateHistory {
  id: number;
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  source: 'MANUAL' | 'API';
  recordedAt: string;
}

export interface ConvertResult {
  from: string;
  to: string;
  amount: number;
  converted: number;
  rate: number;
}

export interface ExchangeRateUpdateDTO {
  fromCurrency: string;
  toCurrency: string;
  rate: number;
}

@Injectable({ providedIn: 'root' })
export class ExchangeRateService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1';

  getAll(): Observable<ExchangeRate[]> {
    return this.http.get<ExchangeRate[]>(`${this.base}/exchange-rates`);
  }

  updateManual(dto: ExchangeRateUpdateDTO): Observable<ExchangeRate> {
    return this.http.put<ExchangeRate>(`${this.base}/admin/exchange-rates`, dto);
  }

  syncFromApi(): Observable<{ message: string; synced: number }> {
    return this.http.post<{ message: string; synced: number }>(
      `${this.base}/admin/exchange-rates/sync`, {}
    );
  }

  getHistory(from?: string, to?: string): Observable<ExchangeRateHistory[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.get<ExchangeRateHistory[]>(
      `${this.base}/admin/exchange-rates/history`, { params }
    );
  }

  convert(from: string, to: string, amount: number): Observable<ConvertResult> {
    const params = new HttpParams().set('from', from).set('to', to).set('amount', amount);
    return this.http.get<ConvertResult>(`${this.base}/exchange-rates/convert`, { params });
  }
}
