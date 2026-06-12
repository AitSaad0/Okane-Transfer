export type StatutAgence = 'ACTIVE' | 'SUSPENDUE';

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
