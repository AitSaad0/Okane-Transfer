import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transfer, PageResponse, TransferSearchParams } from '../../pages/client/transfers/models/client-transfer.model';

@Injectable({ providedIn: 'root' })
export class ClientTransferService {
  private readonly base = '/api/v1';

  constructor(private http: HttpClient) {}

  // CLIENT: mes transferts
  getMyTransfers(params: TransferSearchParams = {}): Observable<PageResponse<Transfer>> {
    let httpParams = new HttpParams();
    if (params.statut) httpParams = httpParams.set('statut', params.statut);
    if (params.dateDebut) httpParams = httpParams.set('dateDebut', params.dateDebut);
    if (params.dateFin) httpParams = httpParams.set('dateFin', params.dateFin);
    httpParams = httpParams.set('page', params.page ?? 0);
    httpParams = httpParams.set('size', params.size ?? 10);
    return this.http.get<PageResponse<Transfer>>(`${this.base}/clients/transfers`, { params: httpParams });  }

  getMyTransferById(id: number): Observable<Transfer> {
    return this.http.get<Transfer>(`${this.base}/clients/transfers/${id}`);
  }

  // PUBLIC: suivi par référence
  trackTransfer(reference: string): Observable<Transfer> {
    return this.http.get<Transfer>(`${this.base}/clients/transfers/track?ref=${reference}`);
  }

  // ADMIN: tous les transferts
  getAllTransfers(params: TransferSearchParams = {}): Observable<PageResponse<Transfer>> {
    let httpParams = new HttpParams();
    if (params.reference) httpParams = httpParams.set('reference', params.reference);
    if (params.statut) httpParams = httpParams.set('statut', params.statut);
    if (params.dateDebut) httpParams = httpParams.set('dateDebut', params.dateDebut);
    if (params.dateFin) httpParams = httpParams.set('dateFin', params.dateFin);
    httpParams = httpParams.set('page', params.page ?? 0);
    httpParams = httpParams.set('size', params.size ?? 10);
    return this.http.get<PageResponse<Transfer>>(`${this.base}/admin/transferts`, { params: httpParams });
  }

  forceCancel(id: number, motif: string): Observable<void> {
    return this.http.post<void>(`${this.base}/admin/transferts/${id}/force-cancel`, { motif });
  }

  downloadReceipt(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/recu/${id}`, { responseType: 'blob' });
  }
}
