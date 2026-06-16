export type TransferStatus = 'EN_ATTENTE' | 'PAYE' | 'ANNULE' | 'EXPIRE';

export interface Transfer {
  id: number;
  reference: string;
  montantEnvoye: number;
  montantRecu: number;
  deviseEnvoi: string;
  deviseReception: string;
  tauxChange: number;
  frais: number;
  statut: TransferStatus;
  dateCreation: string;
  dateModification?: string;
  expediteur: PersonInfo;
  beneficiaire: PersonInfo;
  corridor: string;
  agence?: string;
  agent?: string;
  motif?: string;
}

export interface PersonInfo {
  nom: string;
  prenom: string;
  telephone: string;
  email?: string;
  adresse?: string;
  numeroPiece?: string;
  typePiece?: string;
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
