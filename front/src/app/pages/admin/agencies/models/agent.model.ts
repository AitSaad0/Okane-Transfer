import { Role } from '../../users/models/user.model';

// ── Response DTOs ──────────────────────────────────────────────────────────

export interface AgentDetailResponseDto {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  role: Role;
  active: boolean;

  agenceId: number;
  agenceNom: string;
  agenceVille: string;

  caisseId: string | null;
  caisseOuverte: boolean | null;
  soldeCaisse: number | null;
  dateOuvertureCaisse: string | null;
}

// ── Request DTOs ───────────────────────────────────────────────────────────

export interface AssignAgentRequestDto {
  userId: number;
}

export interface UpdateAgentStatusRequestDto {
  active: boolean;
}
