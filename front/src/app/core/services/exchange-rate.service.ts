import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

// Interface BACK (ce que le back renvoie)
export interface TauxChangeBack {
  id: number;
  taux: number;
  source: string;
  dateMiseAJour: string | null;
  corridorId: number;
  paysOrigineNom: string;
  paysDestinationNom: string;
  deviseSourceCode: string;
  deviseDestinationCode: string;
  deviseSourceSymbole: string;
  deviseDestinationSymbole: string;
}

// Interface FRONT (ce que le template utilise)
export interface ExchangeRate {
  id: number;
  fromCurrency: string;
  toCurrency: string;
  rate: number;
  source: string;
  updatedAt: string | null;
  corridorId: number;
  paysOrigineNom: string;
  paysDestinationNom: string;
}

export interface ConvertResult {
  from: string;
  to: string;
  amount: number;
  converted: number;
  rate: number;
}

export interface ExchangeRateUpdateDTO {
  corridorId: number;
  taux: number;
  source: string;
}

@Injectable({ providedIn: 'root' })
export class ExchangeRateService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1';

  // Mapper back → front
  private toFront(raw: TauxChangeBack): ExchangeRate {
    return {
      id: raw.id,
      fromCurrency: raw.deviseSourceCode,
      toCurrency: raw.deviseDestinationCode,
      rate: raw.taux,
      source: raw.source,
      updatedAt: raw.dateMiseAJour,
      corridorId: raw.corridorId,
      paysOrigineNom: raw.paysOrigineNom,
      paysDestinationNom: raw.paysDestinationNom
    };
  }

  getAll(): Observable<ExchangeRate[]> {
  return this.http.get<TauxChangeBack[]>(`${this.base}/exchange-rates`).pipe(
map(list => list.map(t => this.toFront(t)))
);
}

  updateManual(corridorId: number, taux: number): Observable<ExchangeRate> {
    return this.http.put<TauxChangeBack>(
    `${this.base}/admin/exchange-rates/${corridorId}`,
{ tauxNouveau: taux, source: 'MANUEL' }  // ← "tauxNouveau" = nom du back
).pipe(map(t => this.toFront(t)));
}

  syncFromApi(): Observable<string> {
    return this.http.post(
      `${this.base}/admin/exchange-rates/sync`,
      {},
      { responseType: 'text' }
    );
  }

getHistory(corridorId: number): Observable<ExchangeRate[]> {
return this.http.get<TauxChangeBack[]>(
`${this.base}/admin/exchange-rates/history/${corridorId}`
).pipe(map(list => list.map(t => this.toFront(t))));
}

convert(from: string, to: string, amount: number): Observable<ConvertResult> {
const params = new HttpParams()
.set('from', from)
.set('to', to)
.set('amount', amount);
return this.http.get<ConvertResult>(`${this.base}/exchange-rates/convert`, { params });
}
}
