import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WatchlistEntryResponse, WatchlistEntryRequest, Client } from '../models/watchlist.model';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private readonly kycBase = '/api/v1/admin/kyc';
  private readonly clientBase = '/api/v1/admin/clients';

  constructor(private http: HttpClient) {}

  getWatchlist(): Observable<WatchlistEntryResponse[]> {
    return this.http.get<WatchlistEntryResponse[]>(`${this.kycBase}/watchlist`);
  }

  addToWatchlist(request: WatchlistEntryRequest): Observable<WatchlistEntryResponse> {
    return this.http.post<WatchlistEntryResponse>(`${this.kycBase}/watchlist`, request);
  }

  searchClients(query: string): Observable<Client[]> {
    const params = new HttpParams().set('search', query);
    return this.http.get<Client[]>(this.clientBase, { params });
  }
}
