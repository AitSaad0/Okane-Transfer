import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { JournalAudit, PageResponse } from '../models/journal-audit.model';

@Injectable({ providedIn: 'root' })
export class AuditLogService {

  private readonly baseUrl = '/api/v1/admin/audit-logs';

  constructor(private http: HttpClient) {}

  getAll(
    page: number = 0,
    size: number = 20,
    sort: string = 'timestamp',
    dir: 'asc' | 'desc' = 'desc'
  ): Observable<PageResponse<JournalAudit>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort)
      .set('dir', dir);

    return this.http.get<PageResponse<JournalAudit>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<JournalAudit> {
    return this.http.get<JournalAudit>(`${this.baseUrl}/${id}`);
  }
}
