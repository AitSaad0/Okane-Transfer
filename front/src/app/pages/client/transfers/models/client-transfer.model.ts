export type TransferStatus = 'EN_ATTENTE' | 'PAYE' | 'ANNULE' | 'EXPIRE';

export interface Transfer {
  id: number;
  codeRetrait: string;
  montantEnvoye: number;
  montantRecu: number;
  deviseSource: string;
  deviseDestination: string;
  tauxChange: number;
  frais: number;
  statut: TransferStatus;
  dateCreation: any;
  datePaiement?: any;
  expediteurNom: string;
  expediteurPrenom: string;
  expediteurTelephone: string;
  expediteurEmail: string;
  beneficiaireNom: string;
  beneficiairePrenom: string;
  beneficiaireTelephone: string;
  beneficiaireEmail: string;
  corridorId?: number;
  corridorDescription?: string;
  agenceId?: number;
  agenceNom?: string;
  agentId?: number;
  agentNom?: string;
  agentPrenom?: string;
  flagged?: boolean;
  motif?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface TransferSearchParams {
  reference?: string;
  statut?: TransferStatus;
  dateDebut?: string;
  dateFin?: string;
  page?: number;
  size?: number;
}
