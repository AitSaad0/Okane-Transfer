import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface FeeGrid {
  id: number;
  corridorId: number;
  corridorLabel: string;  // ← On va le construire dans le mapper
  montantMin: number;
  montantMax: number;
  fraisFixe: number;
  pourcentageFrais: number;
  partAgence: number;
  partCentrale: number; // ← 100 - partAgence
}

export interface FeeGridCreateDTO {
  corridorId: number;
  montantMin: number;
  montantMax: number;
  fraisFixe: number;
  pourcentageFrais: number;
  partAgence: number;
}

export interface FeeSimulateRequest {
  corridorId: number;
  amount: number;
}

export interface FeeSimulateResult {
  amount: number;
  fee: number;
  agencyShare: number;
  centralShare: number;
  amountReceived: number;
  corridorLabel: string;
  montantMin: number;
  montantMax: number;
  fraisFixe: number;
  pourcentageFrais: number;
}

@Injectable({ providedIn: 'root' })
export class FeeGridService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/fee-grids';

  getAll(): Observable<FeeGrid[]> {
  return this.http.get<any[]>(this.base).pipe(
    map(list => list.map(g => ({
  id: g.id,
  corridorId: g.corridorId,
  corridorLabel: `${g.corridorPaysOrigineNom || ''} → ${g.corridorPaysDestinationNom || ''}`,
  montantMin: g.montantMin,
  montantMax: g.montantMax,
  fraisFixe: g.fraisFixe,
  pourcentageFrais: g.pourcentageFrais,
  partAgence: g.partAgence,
  partCentrale: 100 - (g.partAgence || 0)
})))
);
}

create(dto: FeeGridCreateDTO): Observable<FeeGrid> {
  return this.http.post<any>(this.base, dto).pipe(
    map(g => ({
      id: g.id,
      corridorId: g.corridorId,
      corridorLabel: `${g.corridorPaysOrigineNom || ''} → ${g.corridorPaysDestinationNom || ''}`,
      montantMin: g.montantMin,
      montantMax: g.montantMax,
      fraisFixe: g.fraisFixe,
      pourcentageFrais: g.pourcentageFrais,
      partAgence: g.partAgence,
      partCentrale: 100 - (g.partAgence || 0)
    }))
  );
}

update(id: number, dto: Partial<FeeGridCreateDTO>): Observable<FeeGrid> {
  return this.http.put<any>(`${this.base}/${id}`, dto).pipe(
    map(g => ({
      id: g.id,
      corridorId: g.corridorId,
      corridorLabel: `${g.corridorPaysOrigineNom || ''} → ${g.corridorPaysDestinationNom || ''}`,
      montantMin: g.montantMin,
      montantMax: g.montantMax,
      fraisFixe: g.fraisFixe,
      pourcentageFrais: g.pourcentageFrais,
      partAgence: g.partAgence,
      partCentrale: 100 - (g.partAgence || 0)
    }))
  );
}

delete(id: number): Observable<void> {
  return this.http.delete<void>(`${this.base}/${id}`);
}

export(format: 'csv' | 'pdf'): Observable<Blob> {
  const params = new HttpParams().set('format', format);
  return this.http.get(`${this.base}/export`, { params, responseType: 'blob' });
}

simulate(req: FeeSimulateRequest): Observable<FeeSimulateResult> {
return this.http.post<FeeSimulateResult>('/api/v1/fees/simulate', req);
}
}
