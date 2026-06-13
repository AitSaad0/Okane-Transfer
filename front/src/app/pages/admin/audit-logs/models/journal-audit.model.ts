export interface JournalAudit {
  id: number;
  action: string;
  details: string | null;
  type: string;
  timestamp: string; // ISO string renvoyé par le backend
  ipAddress: string | null;
  utilisateurId: number;
  utilisateurEmail: string;
  utilisateurNom: string;
  transfertId: number | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  first: boolean;
  last: boolean;
}
