export interface ClientProfile {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse?: string;
  dateNaissance?: string;
  nationalite?: string;
  numeroPiece?: string;
  typePiece?: string;
  kycStatus: 'VERIFIE' | 'EN_ATTENTE' | 'REJETE';
  dateInscription: string;
}

export interface UpdateProfileRequest {
  nom?: string;
  prenom?: string;
  telephone?: string;
  adresse?: string;
}

export interface ClientActivity {
  id: number;
  action: string;
  ipAddress: string;
  userAgent: string;
  dateAction: string;
}

export interface ClientDashboardStats {
  totalTransferts: number;
  totalEnvoye: number;
  transfertsEnCours: number;
  dernierTransfert?: string;
}
