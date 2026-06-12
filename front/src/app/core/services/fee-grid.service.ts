import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FeeSlice {
  minAmount: number;
  maxAmount: number;
  fixedFee: number;
  agencyShare: number;
  centralShare: number;
}

export interface FeeGrid {
  id: number;
  corridorId: number;
  corridorLabel: string;
  slices: FeeSlice[];
  active: boolean;
  createdAt: string;
}

export interface FeeGridCreateDTO {
  corridorId: number;
  slices: FeeSlice[];
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
  appliedSlice: FeeSlice;
}

@Injectable({ providedIn: 'root' })
export class FeeGridService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/admin/fee-grids';

  getAll(): Observable<FeeGrid[]> {
    return this.http.get<FeeGrid[]>(this.base);
  }

  create(dto: FeeGridCreateDTO): Observable<FeeGrid> {
    return this.http.post<FeeGrid>(this.base, dto);
  }

  update(id: number, dto: Partial<FeeGridCreateDTO>): Observable<FeeGrid> {
    return this.http.put<FeeGrid>(`${this.base}/${id}`, dto);
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
