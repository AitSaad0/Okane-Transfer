export interface PaysItem {
  id: number;
  codeIso: string;
  nom: string;
}

export interface Corridor {
  id: number;
  tauxChange: number;
  actif: boolean;
  paysOrigineId: number;
  paysOrigineNom: string;
  paysDestinationId: number;
  paysDestinationNom: string;
  deviseSourceId: number;
  deviseSourceCode: string;
  deviseDestinationId: number;
  deviseDestinationCode: string;
}

export enum OperateurMobile {
  ORANGE_MONEY = 'ORANGE_MONEY',
  WAVE = 'WAVE',
  MPESA = 'MPESA'
}

export interface InfoPersonne {
  nom: string;
  prenom: string;
  telephone: string;
  paysId: number;
  numPieceIdentite?: string;
  email?: string;
}

export interface MobileTransfertRequest {
  operateur: OperateurMobile;
  corridorId: number;
  expediteur: InfoPersonne;
  beneficiaire: InfoPersonne;
  montantEnvoye: number;
}

export interface SimulationRequest {
  corridorId: number;
  montant: number;
}

export interface SimulationResponse {
  montantEnvoye: number;
  fraisFixe: number;
  fraisVariable: number;
  fraisTotal: number;
  montantRecu: number;
  partAgence: number;
  partCentrale: number;
  corridorDescription: string;
  message: string;
}

export interface MobileTransfertResponse {
  id: number;
  reference: string;
  corridorDescription: string;
  dateEnvoi: string;
  operateur: string;
  statut: string;
  expediteurNomComplet: string;
  expediteurTelephone: string;
  expediteurEmail: string;
  expediteurPieceIdentite: string;
  expediteurPays: string;
  beneficiaireNomComplet: string;
  beneficiaireTelephone: string;
  beneficiairePays: string;
  montantDepart: number;
  fraisFixes: number;
  fraisProportionnels: number;
  totalFrais: number;
  montantNetApresFrais: number;
  tauxApplique: string;
  sourceTaux: string;
  montantRecu: number;
  deviseSource: string;
  deviseDestination: string;
}

export interface OperateurOption {
  code: OperateurMobile;
  label: string;
  color: string;
  initial: string;
}
