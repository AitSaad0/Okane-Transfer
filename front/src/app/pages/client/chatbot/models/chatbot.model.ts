export type MessageRole = 'USER' | 'BOT' | 'SYSTEM' | 'ESCALATED';
export type SessionStatus = 'ACTIVE' | 'ARCHIVED' | 'WAITING_AGENT';

export interface ChatbotSession {
  id: number;
  title: string;
  status: SessionStatus;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
  unreadCount: number;
}

export interface ChatbotMessage {
  id: number;
  sessionId: number;
  role: MessageRole;
  content: string;
  createdAt: string;
}

export interface ChatbotRequest {
  sessionId?: number;
  message: string;
}

export interface ChatbotResponse {
  sessionId: number;
  sessionTitle: string;
  message: ChatbotMessage;
  quickReplies: string[];
  escalated: boolean;
}

export interface ChatbotSessionTitle {
  title: string;
}
