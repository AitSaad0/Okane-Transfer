import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatbotRequest, ChatbotResponse, ChatbotSession, ChatbotMessage, ChatbotSessionTitle } from '../models/chatbot.model';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private readonly BASE = '/api/v1/chatbot';

  constructor(private http: HttpClient) {}

  sendMessage(request: ChatbotRequest): Observable<ChatbotResponse> {
    return this.http.post<ChatbotResponse>(`${this.BASE}/message`, request);
  }

  getSessions(status?: string, page?: number, size?: number): Observable<ChatbotSession[]> {
    let params: any = {};
    if (status) params.status = status;
    if (page !== undefined) params.page = page;
    if (size !== undefined) params.size = size;
    return this.http.get<ChatbotSession[]>(`${this.BASE}/sessions`, { params });
  }

  createSession(): Observable<ChatbotSession> {
    return this.http.post<ChatbotSession>(`${this.BASE}/sessions`, {});
  }

  getSessionMessages(sessionId: number, page?: number, size?: number): Observable<any> {
    let params: any = {};
    if (page !== undefined) params.page = page;
    if (size !== undefined) params.size = size;
    return this.http.get<any>(`${this.BASE}/sessions/${sessionId}/messages`, { params });
  }

  updateTitle(sessionId: number, title: string): Observable<ChatbotSession> {
    return this.http.patch<ChatbotSession>(`${this.BASE}/sessions/${sessionId}/title`, { title } as ChatbotSessionTitle);
  }

  deleteSession(sessionId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/sessions/${sessionId}`);
  }

  archiveSession(sessionId: number): Observable<ChatbotSession> {
    return this.http.patch<ChatbotSession>(`${this.BASE}/sessions/${sessionId}/archive`, {});
  }

  restoreSession(sessionId: number): Observable<ChatbotSession> {
    return this.http.patch<ChatbotSession>(`${this.BASE}/sessions/${sessionId}/restore`, {});
  }

  escalateSession(sessionId: number): Observable<ChatbotSession> {
    return this.http.post<ChatbotSession>(`${this.BASE}/sessions/${sessionId}/escalate`, {});
  }
}
