import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AgenceResponseDto, StatutAgence } from '../models/agence.model';
import { PageResponseDto } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class AgenceService {
  private readonly base = '/api/v1/admin/agencies';

  constructor(private http: HttpClient) {}

  getAllAgencesActives(): Observable<PageResponseDto<AgenceResponseDto>> {
    let params = new HttpParams()
      .set('page', 0)
      .set('size', 1000)
      .set('sort', 'nom')
      .set('statut', 'ACTIVE');
    return this.http.get<PageResponseDto<AgenceResponseDto>>(this.base, { params });
  }
}
