import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Currency {
  id: number;
  code: string;
  symbol: string;
  name: string;
  countries: string[];
  active: boolean;
}

export interface CurrencyCreateDTO {
  code: string;
  symbol: string;
  name: string;
  countries: string[];
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CurrencyService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/currencies';

  getAll(page = 0, size = 10, activeOnly?: boolean): Observable<PagedResponse<Currency>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (activeOnly !== undefined) params = params.set('active', activeOnly);
    return this.http.get<PagedResponse<Currency>>(this.base, { params });
  }

  create(dto: CurrencyCreateDTO): Observable<Currency> {
    return this.http.post<Currency>(this.base, dto);
  }

  update(id: number, dto: Partial<CurrencyCreateDTO>): Observable<Currency> {
    return this.http.put<Currency>(`${this.base}/${id}`, dto);
  }

  toggleStatus(id: number, active: boolean): Observable<Currency> {
    return this.http.patch<Currency>(`${this.base}/${id}/status`, { active });
  }
}
