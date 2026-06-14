export type UserRole = 'ADMIN' | 'MANAGER' | 'AGENT' | 'CLIENT';
export type Role = UserRole;

export interface UserResponseDTO {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  telephone: string | null;
  role: UserRole;
  active: boolean;
  agenceId: number | null;
  agenceNom: string | null;
}

export interface CreateUserRequestDto {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  telephone?: string;
  role: UserRole;
  agenceId?: number;
}

export interface UpdateUserRequestDto {
  nom: string;
  prenom: string;
  telephone?: string;
  agenceId?: number;
}

export interface UpdateUserStatusRequestDto {
  active: boolean;
}

export interface ClientActivityResponseDto {
  action: string;
  details: string | null;
  ipAddress: string | null;
  timestamp: string;
  type: string;
}
