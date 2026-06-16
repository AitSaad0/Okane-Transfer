// manager-dashboard.model.ts
export interface AgentStatDTO {
  agentId: number;
  agentNom: string;
  agentPrenom: string;
  nombreTransferts: number;
  volumeTraite: number;
  commissionsGenerees: number;
}

export interface ManagerDashboardResponseDTO {
  volumeJour: number;
  commissionsJour: number;
  tauxSucces: number;
  nombreAgentsActifs: number;
  nombreTransfertsJour: number;
  nombreTransfertsEnAttente: number;
  soldeCaisseTotal: number;
  topAgents: AgentStatDTO[];
  agenceNom: string;
  agencePays: string;
  agenceVille: string;
}

export interface TransfertResponseDTO {
  id: number;
  reference: string;
  expediteurNom: string;
  expediteurPrenom: string;
  beneficiaireNom: string;
  beneficiairePrenom: string;
  montantEnvoye: number;
  frais: number;
  statut: string;
  dateCreation: string;
  agentEnvoiNom?: string;
  codeRetrait?: string;
}

export interface PageResponseDto<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}
