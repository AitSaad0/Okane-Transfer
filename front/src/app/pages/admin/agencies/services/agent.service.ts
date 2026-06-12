import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AgentDetailResponseDto,
  AssignAgentRequestDto,
  UpdateAgentStatusRequestDto,
} from '../models/agent.model';

@Injectable({ providedIn: 'root' })
export class AgentService {
  private base(agencyId: number) {
    return `/api/v1/agencies/${agencyId}/agents`;
  }

  constructor(private http: HttpClient) {}

  getAgentsByAgence(agencyId: number): Observable<AgentDetailResponseDto[]> {
    return this.http.get<AgentDetailResponseDto[]>(this.base(agencyId));
  }

  assignAgent(agencyId: number, dto: AssignAgentRequestDto): Observable<AgentDetailResponseDto> {
    return this.http.post<AgentDetailResponseDto>(this.base(agencyId), dto);
  }

  removeAgent(agencyId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(agencyId)}/${userId}`);
  }

  updateAgentStatus(
    agencyId: number,
    userId: number,
    dto: UpdateAgentStatusRequestDto,
  ): Observable<AgentDetailResponseDto> {
    return this.http.patch<AgentDetailResponseDto>(`${this.base(agencyId)}/${userId}/status`, dto);
  }
}
