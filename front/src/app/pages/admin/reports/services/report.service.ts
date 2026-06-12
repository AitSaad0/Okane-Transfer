import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private readonly BASE = '/api/v1/admin/reports';

  constructor(private http: HttpClient) {}

  getDailyReport(date: string, corridorId?: string, agenceId?: string): Observable<any> {
    let params = new HttpParams().set('date', date);
    if (corridorId) params = params.set('corridorId', corridorId);
    if (agenceId) params = params.set('agenceId', agenceId);
    return this.http.get(`${this.BASE}/daily`, { params });
  }

  getMonthlyReport(year: number): Observable<any> {
    return this.http.get(`${this.BASE}/monthly`, {
      params: new HttpParams().set('year', year)
    });
  }

  getCorridorReport(startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.BASE}/corridors`, {
      params: new HttpParams().set('startDate', startDate).set('endDate', endDate)
    });
  }

  exportDailyReport(format: string, date: string, corridorId?: string) {
    let params = new HttpParams().set('format', format).set('date', date);
    if (corridorId) params = params.set('corridorId', corridorId);
    return this.http.get(`${this.BASE}/export/daily`, { params, responseType: 'blob' });
  }

  exportMonthlyReport(format: string, year: number) {
    const params = new HttpParams().set('format', format).set('year', year);
    return this.http.get(`${this.BASE}/export/monthly`, { params, responseType: 'blob' });
  }

  exportCorridorReport(format: string, startDate: string, endDate: string) {
    const params = new HttpParams()
      .set('format', format)
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get(`${this.BASE}/export/corridors`, { params, responseType: 'blob' });
  }
}
