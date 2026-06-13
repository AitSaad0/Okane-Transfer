export interface WatchlistEntryResponse {
  id: string;
  fullName: string;
  idNumber?: string;
  source: string;
  reason?: string;
  addedAt: string;
  addedBy: string;
}

export interface WatchlistEntryRequest {
  fullName: string;
  idNumber?: string;
  source: string;
  reason?: string;
}

export interface Client {
  id: number;
  nom: string;
  prenom: string;
  numPieceIdentite: string;
  telephone: string;
  email: string;
  estSurListeSurveillance: boolean;
}
