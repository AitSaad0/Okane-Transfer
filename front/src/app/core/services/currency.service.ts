import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';

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
  symbole: string;
  nom: string;
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

  private toFront(raw: any): Currency {
    return {
      id: raw.id ?? 0,
      code: raw.code ?? '',
      symbol: raw.symbole ?? '',
      name: raw.nom ?? '',
      countries: raw.countries ?? [],
      active: raw.active ?? true
    };
  }

  private toBack(dto: Partial<CurrencyCreateDTO>): any {
    return {
      code: dto.code,
      symbole: dto.symbole,
      nom: dto.nom,
      countries: dto.countries
    };
  }

  getAll(page = 0, size = 10, activeOnly?: boolean): Observable<PagedResponse<Currency>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (activeOnly !== undefined) params = params.set('active', activeOnly);

    return this.http.get<any>(this.base, { params }).pipe(
      map(response => {
        // Le back renvoie un tableau simple []
        if (Array.isArray(response)) {
          return {
            content: response.map(c => this.toFront(c)),
            totalElements: response.length,
            totalPages: 1,
            number: 0,
            size: response.length
          };
        }
        // Ou un objet paginé (futur)
        return {
          content: (response.content ?? []).map((c: any) => this.toFront(c)),
          totalElements: response.totalElements ?? 0,
          totalPages: response.totalPages ?? 1,
          number: response.number ?? 0,
          size: response.size ?? 10
        };
      }),
      catchError(err => {
        console.error('getAll error:', err);
        return of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
      })
    );
  }

  create(dto: CurrencyCreateDTO): Observable<Currency> {
    return this.http.post<any>(this.base, this.toBack(dto)).pipe(
      map(c => this.toFront(c))
    );
  }

  update(id: number, dto: Partial<CurrencyCreateDTO>): Observable<Currency> {
    return this.http.put<any>(`${this.base}/${id}`, this.toBack(dto)).pipe(
      map(c => this.toFront(c))
    );
  }

  toggleStatus(id: number, active: boolean): Observable<Currency> {
    return this.http.patch<any>(`${this.base}/${id}/status`, { active }).pipe(
      map(c => this.toFront(c))
    );
  }
}
