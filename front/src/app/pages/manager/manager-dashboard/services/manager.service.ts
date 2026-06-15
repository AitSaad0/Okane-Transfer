// manager.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ManagerDashboardResponseDTO, PageResponseDto, TransfertResponseDTO } from '../models/manager-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class ManagerService {
  private baseUrl = '/api/v1/manager';

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<ManagerDashboardResponseDTO> {
    return this.http.get<ManagerDashboardResponseDTO>(`${this.baseUrl}/dashboard`);
  }

  getRapportJournalier(date?: string): Observable<ManagerDashboardResponseDTO> {
    let params = new HttpParams();
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get<ManagerDashboardResponseDTO>(`${this.baseUrl}/reports/daily`, { params });
  }

  getTransfertsAgence(statut?: string, page: number = 0, size: number = 20): Observable<PageResponseDto<TransfertResponseDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (statut) {
      params = params.set('statut', statut);
    }
    return this.http.get<PageResponseDto<TransfertResponseDTO>>(`${this.baseUrl}/transfers`, { params });
  }

  exportRapport(format: 'csv' | 'pdf', date?: string): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    if (date) {
      params = params.set('date', date);
    }
    return this.http.get(`${this.baseUrl}/reports/export`, {
      params,
      responseType: 'blob'
    });
  }
}
