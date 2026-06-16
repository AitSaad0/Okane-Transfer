import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Country {
  id: number;
  code: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class CountryService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/countries';

  getAll(): Observable<Country[]> {
    return this.http.get<any[]>(this.base).pipe(
      map((list) =>
        list.map((p) => ({
          id: p.id,
          code: p.codeIso,
          name: p.nom,
        })),
      ),
    );
  }
}
