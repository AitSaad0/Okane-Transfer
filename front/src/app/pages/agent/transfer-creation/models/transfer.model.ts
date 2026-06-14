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

export interface InfoPersonne {
  nom: string;
  prenom: string;
  telephone: string;
  paysId: number;
  numPieceIdentite?: string;
  email?: string;
}

export interface TransfertRequest {
  expediteur: InfoPersonne;
  beneficiaire: InfoPersonne;
  montantEnvoye: number;
  corridorId: number;
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

export interface TransfertResponse {
  id: number;
  codeRetrait: string;
  montantEnvoye: number;
  frais: number;
  montantNet: number;
  statut: string;
  expediteurNom: string;
  expediteurPrenom: string;
  expediteurTelephone: string;
  beneficiaireNom: string;
  beneficiairePrenom: string;
  beneficiaireTelephone: string;
  corridorDescription: string;
  deviseSource: string;
  deviseDestination: string;
  agentNom: string;
  agentPrenom: string;
  dateCreation: string;
}
