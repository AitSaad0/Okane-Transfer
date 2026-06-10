export interface PaiementResponse {
  id: number;
  codeRetrait: string;
  reference: string;
  corridorDescription: string;
  dateEnvoi: string;
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
  montantNet: number;

  tauxChange: number;
  sourceTaux: string;

  montantRecu: number;
  deviseSource: string;
  deviseDestination: string;

  dateCreation: string;
  datePaiement: string;

  agentEnvoiNom: string;
  agentEnvoiPrenom: string;
  agentPaiementNom: string;
  agentPaiementPrenom: string;

  paye: boolean;
}

export interface PaiementRequest {
  transfertId: number;
  pieceIdentiteBeneficiaire: string;
  codeRetrait: string;
}
