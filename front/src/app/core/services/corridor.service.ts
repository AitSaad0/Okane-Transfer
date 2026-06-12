import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Corridor {
  id: number;
  sourceCountry: string;
  destinationCountry: string;
  sourceCurrencyCode: string;
  destinationCurrencyCode: string;
  active: boolean;
}

export interface CorridorCreateDTO {
  sourceCountry: string;
  destinationCountry: string;
  sourceCurrencyCode: string;
  destinationCurrencyCode: string;
}

@Injectable({ providedIn: 'root' })
export class CorridorService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/corridors';

  getAll(): Observable<Corridor[]> {
    return this.http.get<Corridor[]>(this.base);
  }

  getActive(): Observable<Corridor[]> {
    return this.http.get<Corridor[]>('/api/v1/corridors/active');
  }

  create(dto: CorridorCreateDTO): Observable<Corridor> {
    return this.http.post<Corridor>(this.base, dto);
  }

  toggleStatus(id: number, active: boolean): Observable<Corridor> {
    return this.http.patch<Corridor>(`${this.base}/${id}/status`, { active });
  }
}
