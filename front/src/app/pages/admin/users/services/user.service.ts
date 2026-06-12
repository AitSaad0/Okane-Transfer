import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UserResponseDTO,
  CreateUserRequestDto,
  UpdateUserRequestDto,
  UpdateUserStatusRequestDto,
  ClientActivityResponseDto,
  Role,
} from '../models/user.model';
import { PageResponseDto } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly base = '/api/v1/admin/users';
  private readonly clientBase = '/api/v1/clients';

  constructor(private http: HttpClient) {}

  getAllUsers(
    page = 0,
    size = 20,
    sort = 'id',
    filters: { role?: Role; active?: boolean; agenceId?: number } = {},
  ): Observable<PageResponseDto<UserResponseDTO>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    if (filters.role != null) params = params.set('role', filters.role);
    if (filters.active != null) params = params.set('active', String(filters.active));
    if (filters.agenceId != null) params = params.set('agenceId', filters.agenceId);
    return this.http.get<PageResponseDto<UserResponseDTO>>(this.base, { params });
  }

  getUserById(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.base}/${id}`);
  }

  createUser(dto: CreateUserRequestDto): Observable<UserResponseDTO> {
    return this.http.post<UserResponseDTO>(this.base, dto);
  }

  updateUser(id: number, dto: UpdateUserRequestDto): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.base}/${id}`, dto);
  }

  updateStatus(id: number, dto: UpdateUserStatusRequestDto): Observable<UserResponseDTO> {
    return this.http.patch<UserResponseDTO>(`${this.base}/${id}/status`, dto);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getActivity(id: number): Observable<ClientActivityResponseDto[]> {
    return this.http.get<ClientActivityResponseDto[]>(`${this.clientBase}/profile/activity`);
  }
}
