import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Corridor {
  id: number;
  sourceCountry: string;
  destinationCountry: string;
  sourceCurrencyCode: string;
  destinationCurrencyCode: string;
  active: boolean;
}

export interface CorridorCreateDTO {
  paysSourceCode: string;
  paysDestinationCode: string;
  deviseSourceCode: string;
  deviseDestinationCode: string;
}

@Injectable({ providedIn: 'root' })
export class CorridorService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/corridors';

  getAll(): Observable<Corridor[]> {
  return this.http.get<any[]>(this.base).pipe(
    map(list => list.map(c => ({
  id: c.id,
  sourceCountry: c.paysOrigineNom || '-',
  destinationCountry: c.paysDestinationNom || '-',
  sourceCurrencyCode: c.deviseSourceCode || '-',
  destinationCurrencyCode: c.deviseDestinationCode || '-',
  active: c.actif ?? true
})))
);
}

  getActive(): Observable<Corridor[]> {
  return this.http.get<any[]>('/api/v1/admin/corridors').pipe(
    map((list) =>
  list.map((c) => ({
  id: c.id,
  sourceCountry: c.paysOrigineNom || '-',
  destinationCountry: c.paysDestinationNom || '-',
  sourceCurrencyCode: c.deviseSourceCode || '-',
  destinationCurrencyCode: c.deviseDestinationCode || '-',
  active: c.actif ?? true,
})),
),
);
}

  create(dto: CorridorCreateDTO): Observable<Corridor> {
    return this.http.post<any>(`${this.base}/by-code`, dto).pipe(
      map((c) => ({
        id: c.id,
        sourceCountry: c.paysOrigine?.nom || dto.paysSourceCode,
        destinationCountry: c.paysDestination?.nom || dto.paysDestinationCode,
        sourceCurrencyCode: c.deviseSource?.code || dto.deviseSourceCode,
        destinationCurrencyCode: c.deviseDestination?.code || dto.deviseDestinationCode,
        active: c.actif ?? true,
      })),
    );
  }

  toggleStatus(id: number, active: boolean): Observable<Corridor> {
    return this.http.patch<any>(`${this.base}/${id}/status`, { active }).pipe(
      map((c) => ({
        id: c.id,
        sourceCountry: c.paysOrigine?.nom || '',
        destinationCountry: c.paysDestination?.nom || '',
        sourceCurrencyCode: c.deviseSource?.code || '',
        destinationCurrencyCode: c.deviseDestination?.code || '',
        active: c.actif ?? true,
      })),
    );
  }
}
