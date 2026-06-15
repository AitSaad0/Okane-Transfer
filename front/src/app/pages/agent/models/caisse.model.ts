export type CaisseStatus = 'OUVERTE' | 'FERMEE';

export interface Caisse {
  id: number;
  agentId: number;
  agentNom: string;
  agenceNom: string;
  soldeOuverture: number;
  soldeCourant: number;
  devise: string;
  statut: CaisseStatus;
  dateOuverture: string;
  dateCloture?: string;
  nombreTransferts: number;
  totalPaie: number;
  totalEnvoye: number;
}

export interface ClotureCaisseRequest {
  soldeDeclareFermeture: number;
  commentaire?: string;
}

export interface EcartCaisseRequest {
  soldeReel: number;
  soldeSysteme: number;
  motif: string;
  justificatif?: string;
}

export interface OperationCaisse {
  id: number;
  type: 'TRANSFERT_PAYE' | 'TRANSFERT_ENVOYE' | 'AJUSTEMENT';
  montant: number;
  devise: string;
  reference?: string;
  dateOperation: string;
  description: string;
}
