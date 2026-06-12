import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaiementResponse, PaiementRequest } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly BASE = '/api/v1/agent/transfers';

  constructor(private http: HttpClient) {}

  searchByCode(codeRetrait: string): Observable<PaiementResponse> {
    return this.http.get<PaiementResponse>(`${this.BASE}/search/code`, {
      params: { codeRetrait }
    });
  }

  searchByTelephone(telephone: string): Observable<PaiementResponse[]> {
    return this.http.get<PaiementResponse[]>(`${this.BASE}/search/telephone`, {
      params: { telephone }
    });
  }

  confirmPayment(id: number, request: PaiementRequest): Observable<PaiementResponse> {
    return this.http.post<PaiementResponse>(`${this.BASE}/${id}/payer`, request);
  }

  downloadPaymentReceipt(id: number): Observable<Blob> {
    return this.http.get(`${this.BASE}/${id}/recu-paiement`, {
      responseType: 'blob'
    });
  }
}
