export type StatutAgence = 'ACTIVE' | 'SUSPENDUE';

// ── Pays ────────────────────────────────────────────────────────────────
export interface PaysResponseDTO {
  id: number;
  codeIso: string;
  nom: string;
}

// ── Agence ──────────────────────────────────────────────────────────────
export interface AgenceResponseDto {
  id: number;
  nom: string;
  adresse: string;
  ville: string;
  codePostal: string;
  plafondJournalier: number;
  statut: StatutAgence;
  paysNom: string;
  paysCode: string;
}

export interface AgenceDashboardResponseDto {
  agenceId: number;
  agenceNom: string;
  agenceVille: string;
  paysNom: string;
  volumeEnvoiJour: number;
  volumePaiementJour: number;
  nombreTransfertJour: number;
  tauxSucces: number;
  transfertsPaye: number;
  transfertsEnAttente: number;
  transfertsAnnule: number;
  transfertsExpire: number;
  commissionsGenerees: number;
  plafondJournalier: number;
  tauxUtilisationPlafond: number;
  soldeCaisseActuel: number;
  caisseOuverte: boolean;
}

// ── Request DTOs ────────────────────────────────────────────────────────
export interface CreateAgenceRequestDto {
  nom: string;
  adresse: string;
  ville: string;
  codePostal: string;
  plafondJournalier: number;
  paysId: number;
}

export interface UpdateAgenceRequestDto {
  nom: string;
  adresse: string;
  ville: string;
  codePostal: string;
  plafondJournalier: number;
  paysId: number;
}

export interface UpdateAgenceStatusRequestDto {
  statut: StatutAgence;
}

// ── Agents ──────────────────────────────────────────────────────────────
export type AgentRole = 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_AGENT' | 'ROLE_CLIENT';

export interface AgentDetailResponseDto {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  role: AgentRole;
  active: boolean;

  agenceId: number;
  agenceNom: string;
  agenceVille: string;

  caisseId: string | null;
  caisseOuverte: boolean | null;
  soldeCaisse: number | null;
  dateOuvertureCaisse: string | null;
}

export interface AssignAgentRequestDto {
  userId: number;
}

export interface UpdateAgentStatusRequestDto {
  active: boolean;
}
