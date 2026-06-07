import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  // ✅ proxy handles backend routing
  private baseUrl = '/api/v1/admin/reports';

  constructor(private http: HttpClient) {}

  getDailyReport(date: string, corridorId?: string, agenceId?: string): Observable<any> {
    let params = new HttpParams().set('date', date);

    if (corridorId) params = params.set('corridorId', corridorId);
    if (agenceId) params = params.set('agenceId', agenceId);

    return this.http.get(`${this.baseUrl}/daily`, { params });
  }

  getMonthlyReport(year: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/monthly`, {
      params: new HttpParams().set('year', year)
    });
  }

  getCorridorReport(startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/corridors`, {
      params: new HttpParams()
        .set('startDate', startDate)
        .set('endDate', endDate)
    });
  }

  exportReport(format: string, date: string, corridorId?: string) {
    let params = new HttpParams()
      .set('format', format)
      .set('date', date);

    if (corridorId) params = params.set('corridorId', corridorId);

    return this.http.get(`${this.baseUrl}/export`, {
      params,
      responseType: 'blob'
    });
  }
}
