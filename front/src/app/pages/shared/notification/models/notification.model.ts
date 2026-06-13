// notification.model.ts
export type CanalNotification = 'EMAIL' | 'SMS' | 'PUSH';

export type TypeNotification =
  | 'TRANSFERT_CREE'
  | 'TRANSFERT_PAYE'
  | 'TRANSFERT_ANNULE'
  | 'TRANSFERT_EXPIRE'
  | 'RECU_EXPEDITEUR'
  | 'CONFIRMATION_RETRAIT'
  | 'SYSTEME'
  | 'MAINTENANCE'
  | 'ALERTE';

export interface NotificationResponseDto {
  id: number;
  type: TypeNotification;
  canal: CanalNotification;
  contenu: string;
  lu: boolean;
  dateEnvoi: string; // ISO string from backend
}
