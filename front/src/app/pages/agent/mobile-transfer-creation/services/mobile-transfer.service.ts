import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MobileTransfertRequest, MobileTransfertResponse, Corridor, PaysItem, SimulationRequest, SimulationResponse } from '../models/mobile-transfer.model';

@Injectable({ providedIn: 'root' })
export class MobileTransferService {
  private readonly BASE = '/api/v1/agent/transfers/mobile';
  private readonly CORRIDOR_BASE = '/api/v1/admin/corridors';

  constructor(private http: HttpClient) {}

  creerTransfertMobile(request: MobileTransfertRequest): Observable<MobileTransfertResponse> {
    return this.http.post<MobileTransfertResponse>(this.BASE, request);
  }

  getActiveCorridors(): Observable<Corridor[]> {
    return this.http.get<Corridor[]>(`${this.CORRIDOR_BASE}/active`);
  }

  getAllCountries(): Observable<PaysItem[]> {
    return this.http.get<PaysItem[]>('/api/v1/admin/countries');
  }

  simulateFees(request: SimulationRequest): Observable<SimulationResponse> {
    return this.http.post<SimulationResponse>('/api/v1/fees/simulate', request);
  }
}
