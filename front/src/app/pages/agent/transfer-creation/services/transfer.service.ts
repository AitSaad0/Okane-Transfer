import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransfertRequest, TransfertResponse, Corridor, PaysItem, SimulationRequest, SimulationResponse } from '../models/transfer.model';

@Injectable({ providedIn: 'root' })
export class TransferService {
  private readonly BASE = '/api/v1/agent/transfers';
  private readonly CORRIDOR_BASE = '/api/v1/admin/corridors';

  constructor(private http: HttpClient) {}

  creerTransfert(request: TransfertRequest): Observable<TransfertResponse> {
    return this.http.post<TransfertResponse>(this.BASE, request);
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
