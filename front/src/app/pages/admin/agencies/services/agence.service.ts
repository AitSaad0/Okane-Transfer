import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AgenceDashboardResponseDto,
  AgenceResponseDto,
  CreateAgenceRequestDto,
  PaysResponseDTO,
  StatutAgence,
  UpdateAgenceRequestDto,
  UpdateAgenceStatusRequestDto,
} from '../models/agence.model';
import { PageResponseDto } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class AgenceService {
  private readonly base = '/api/v1/admin/agencies';
  private readonly paysBase = '/api/v1/admin/countries';
  constructor(private http: HttpClient) {}

  getAllAgencesActives(): Observable<PageResponseDto<AgenceResponseDto>> {
    let params = new HttpParams()
      .set('page', 0)
      .set('size', 1000)
      .set('sort', 'nom')
      .set('statut', 'ACTIVE');
    return this.http.get<PageResponseDto<AgenceResponseDto>>(this.base, { params });
  }

  getAllAgences(
    page = 0,
    size = 20,
    sort = 'id',
    filters: { paysId?: number; statut?: StatutAgence } = {},
  ): Observable<PageResponseDto<AgenceResponseDto>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    if (filters.paysId != null) params = params.set('paysId', filters.paysId);
    if (filters.statut != null) params = params.set('statut', filters.statut);
    return this.http.get<PageResponseDto<AgenceResponseDto>>(this.base, { params });
  }

  getAgenceById(id: number): Observable<AgenceResponseDto> {
    return this.http.get<AgenceResponseDto>(`${this.base}/${id}`);
  }

  createAgence(dto: CreateAgenceRequestDto): Observable<AgenceResponseDto> {
    return this.http.post<AgenceResponseDto>(this.base, dto);
  }

  updateAgence(id: number, dto: UpdateAgenceRequestDto): Observable<AgenceResponseDto> {
    return this.http.put<AgenceResponseDto>(`${this.base}/${id}`, dto);
  }

  updateStatus(id: number, dto: UpdateAgenceStatusRequestDto): Observable<AgenceResponseDto> {
    return this.http.patch<AgenceResponseDto>(`${this.base}/${id}/status`, dto);
  }

  getDashboard(id: number): Observable<AgenceDashboardResponseDto> {
    return this.http.get<AgenceDashboardResponseDto>(`${this.base}/${id}/dashboard`);
  }

  // Liste des pays pour le select du formulaire
  // ⚠️ Endpoint à confirmer/créer côté backend (pas listé dans le CDC)
  getAllPays(): Observable<PaysResponseDTO[]> {
    return this.http.get<PaysResponseDTO[]>(this.paysBase);
  }
}
