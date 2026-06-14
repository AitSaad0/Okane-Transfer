export enum TypeNotification {
  TRANSFERT_CREE = 'TRANSFERT_CREE',
  TRANSFERT_PAYE = 'TRANSFERT_PAYE',
  TRANSFERT_ANNULE = 'TRANSFERT_ANNULE',
  TRANSFERT_EXPIRE = 'TRANSFERT_EXPIRE',
  RECU_EXPEDITEUR = 'RECU_EXPEDITEUR',
  CONFIRMATION_RETRAIT = 'CONFIRMATION_RETRAIT',
  SYSTEME = 'SYSTEME',
  MAINTENANCE = 'MAINTENANCE',
  ALERTE = 'ALERTE'
}

export enum CanalNotification {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  PUSH = 'PUSH'
}

export interface BroadcastNotificationRequest {
  type: TypeNotification;
  canal: CanalNotification;
  contenu: string;
}

export interface BroadcastNotificationResponse {
  type: TypeNotification;
  canal: CanalNotification;
  contenu: string;
  dateEnvoi: Date;
}
