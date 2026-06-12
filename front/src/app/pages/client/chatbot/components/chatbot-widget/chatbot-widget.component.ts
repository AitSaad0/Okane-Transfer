import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatbotSession, ChatbotMessage, ChatbotResponse } from '../../models/chatbot.model';
import { ChatbotSessionItemComponent } from '../chatbot-session-item/chatbot-session-item.component';

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule, ChatbotSessionItemComponent],
  template: `
    <div class="fixed bottom-6 right-6 z-50">
      <button
        *ngIf="!isOpen"
        (click)="toggle()"
        class="w-14 h-14 bg-primary text-white rounded-full shadow-lg hover:bg-primary-dark transition-all duration-300 flex items-center justify-center text-2xl"
      >💬</button>

      <div
        *ngIf="isOpen"
        class="absolute bottom-0 right-0 w-96 h-[32rem] bg-white rounded-2xl shadow-2xl border border-slate-200 flex flex-col transition-all duration-300"
      >
        <ng-container *ngIf="!showHistory; else historyView">
          <div class="flex items-center justify-between px-4 py-3 border-b border-slate-200 rounded-t-2xl bg-slate-50">
            <span class="font-semibold text-slate-800 truncate">{{ currentSession?.title || ('CHATBOT.TITLE' | translate) }}</span>
            <div class="flex items-center gap-2 shrink-0">
              <button
                (click)="showHistory = true; loadSessions()"
                class="text-sm text-slate-500 hover:text-slate-700 px-2 py-1 rounded hover:bg-slate-200 transition-colors"
              >☰</button>
              <button
                (click)="newSession()"
                class="text-sm text-slate-500 hover:text-slate-700 px-2 py-1 rounded hover:bg-slate-200 transition-colors"
              >+</button>
              <button
                (click)="toggle()"
                class="text-slate-400 hover:text-slate-600 text-xl leading-none"
              >&times;</button>
            </div>
          </div>

          <div #messageContainer (scroll)="onScroll()" class="flex-1 overflow-y-auto p-4 space-y-3">
            <div *ngFor="let msg of messages" class="flex" [class.justify-end]="msg.role === 'USER'" [class.justify-start]="msg.role !== 'USER'">
              <div
                class="max-w-[80%] px-3 py-2 rounded-lg text-sm whitespace-pre-wrap"
                [class.bg-primary]="msg.role === 'USER'"
                [class.text-white]="msg.role === 'USER'"
                [class.bg-slate-100]="msg.role === 'BOT'"
                [class.text-slate-800]="msg.role === 'BOT'"
                [class.bg-yellow-50]="msg.role === 'SYSTEM'"
                [class.text-yellow-700]="msg.role === 'SYSTEM'"
                [class.bg-red-50]="msg.role === 'ESCALATED'"
                [class.text-red-600]="msg.role === 'ESCALATED'"
              >{{ msg.content }}</div>
            </div>

            <div *ngIf="isLoadingMessages" class="flex justify-start">
              <div class="bg-slate-100 px-3 py-2 rounded-lg text-sm text-slate-500 flex items-center gap-1">
                <span class="typing-dot">.</span><span class="typing-dot">.</span><span class="typing-dot">.</span>
              </div>
            </div>

            <div *ngIf="isTyping" class="flex justify-start">
              <div class="bg-slate-100 px-3 py-2 rounded-lg text-sm text-slate-500 flex items-center gap-1">
                <span class="typing-dot">.</span><span class="typing-dot">.</span><span class="typing-dot">.</span>
              </div>
            </div>

            <div *ngIf="quickReplies.length > 0" class="flex flex-wrap gap-2 pt-2">
              <button
                *ngFor="let qr of quickReplies"
                (click)="sendQuickReply(qr)"
                class="text-xs bg-slate-100 text-slate-600 px-3 py-1.5 rounded-full border border-slate-200 hover:bg-primary hover:text-white hover:border-primary transition-all"
              >{{ qr }}</button>
            </div>

            <div *ngIf="waitingForAgent" class="flex justify-start">
              <div class="bg-orange-50 border border-orange-200 text-orange-700 px-4 py-3 rounded-lg text-sm flex items-center gap-2">
                <span class="animate-pulse">⏳</span>
                <span>En attente de la réponse de l'agent humain...</span>
              </div>
            </div>
          </div>

          <div *ngIf="currentSession && !waitingForAgent" class="px-4 pt-2">
            <button
              (click)="escalateToAgent()"
              [disabled]="isLoading"
              class="w-full text-sm bg-orange-50 text-orange-600 border border-orange-200 rounded-lg px-3 py-2 hover:bg-orange-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-center"
            >📞 Contacter un agent humain</button>
          </div>

          <div class="px-4 py-3 border-t border-slate-200">
            <div class="flex items-center gap-2">
              <input
                [(ngModel)]="newMessage"
                (keyup.enter)="sendMessage()"
                placeholder="{{ 'CHATBOT.INPUT_PLACEHOLDER' | translate }}"
                [disabled]="waitingForAgent"
                class="flex-1 border border-slate-300 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition-colors disabled:bg-slate-100 disabled:cursor-not-allowed"
              />
              <button
                (click)="sendMessage()"
                [disabled]="!newMessage.trim() || isLoading || waitingForAgent"
                class="w-10 h-10 bg-primary text-white rounded-full flex items-center justify-center hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >➤</button>
            </div>
          </div>
        </ng-container>

        <ng-template #historyView>
          <div class="flex items-center justify-between px-4 py-3 border-b border-slate-200 rounded-t-2xl bg-slate-50">
            <span class="font-semibold text-slate-800">{{ 'CHATBOT.SESSION_LIST_TITLE' | translate }}</span>
            <div class="flex items-center gap-2">
              <button
                (click)="showHistory = false"
                class="text-sm text-slate-500 hover:text-slate-700 px-2 py-1 rounded hover:bg-slate-200 transition-colors"
              >{{ 'CHATBOT.BACK' | translate }}</button>
              <button
                (click)="toggle()"
                class="text-slate-400 hover:text-slate-600 text-xl leading-none"
              >&times;</button>
            </div>
          </div>

          <div class="flex border-b border-slate-200">
            <button
              *ngFor="let f of historyFilters"
              (click)="setHistoryFilter(f.key)"
              class="flex-1 py-2 text-sm font-medium transition-colors"
              [class.text-primary]="activeHistoryFilter === f.key"
              [class.border-b-2]="activeHistoryFilter === f.key"
              [class.border-primary]="activeHistoryFilter === f.key"
              [class.text-slate-500]="activeHistoryFilter !== f.key"
            >{{ f.label | translate }}</button>
          </div>

          <div class="flex-1 overflow-y-auto">
            <div *ngIf="filteredSessions.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400 text-sm px-4 text-center">
              <p class="mb-2 text-lg">📭</p>
              <p>{{ 'CHATBOT.NO_SESSIONS' | translate }}</p>
              <button
                (click)="newSession()"
                class="mt-3 text-sm bg-primary text-white px-4 py-1.5 rounded hover:bg-primary-dark transition-colors"
              >+ {{ 'CHATBOT.NEW_CHAT' | translate }}</button>
            </div>
            <app-chatbot-session-item
              *ngFor="let s of filteredSessions"
              [session]="s"
              (selected)="selectSession($event)"
              (archived)="archiveSession($event)"
              (restored)="restoreSession($event)"
              (deleted)="deleteSession($event)"
              (titleChanged)="updateTitle($event)"
            ></app-chatbot-session-item>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    @keyframes typing { 0%, 100% { opacity: 0.3; } 50% { opacity: 1; } }
    .typing-dot { animation: typing 1.4s infinite both; font-size: 1.5rem; line-height: 0; }
    .typing-dot:nth-child(2) { animation-delay: 0.2s; }
    .typing-dot:nth-child(3) { animation-delay: 0.4s; }
  `]
})
export class ChatbotWidgetComponent implements OnInit {
  @ViewChild('messageContainer') private messageContainer!: ElementRef<HTMLElement>;
  private userScrolledUp = false;

  isOpen = false;
  showHistory = false;
  isTyping = false;
  isLoading = false;
  isLoadingMessages = false;
  waitingForAgent = false;
  newMessage = '';
  messages: ChatbotMessage[] = [];
  quickReplies: string[] = [];
  currentSession?: ChatbotSession;
  allSessions: ChatbotSession[] = [];

  historyFilters = [
    { key: 'ALL', label: 'CHATBOT.FILTER_ALL' },
    { key: 'ACTIVE', label: 'CHATBOT.FILTER_ACTIVE' },
    { key: 'ARCHIVED', label: 'CHATBOT.FILTER_ARCHIVED' }
  ];
  activeHistoryFilter = 'ALL';

  get filteredSessions(): ChatbotSession[] {
    if (this.activeHistoryFilter === 'ALL') return this.allSessions;
    if (this.activeHistoryFilter === 'ACTIVE') return this.allSessions.filter(s => s.status === 'ACTIVE' || s.status === 'WAITING_AGENT');
    return this.allSessions.filter(s => s.status === this.activeHistoryFilter);
  }

  constructor(
    private chatbotService: ChatbotService,
    private cdr: ChangeDetectorRef
  ) {}

  private scrollToBottom() {
    setTimeout(() => {
      if (!this.messageContainer) return;
      const el = this.messageContainer.nativeElement;
      const saved = this.getSavedScroll();
      if (saved > 0 && saved < el.scrollHeight) {
        el.scrollTop = saved;
        this.userScrolledUp = el.scrollHeight - saved - el.clientHeight > 30;
      } else {
        el.scrollTop = el.scrollHeight;
        this.userScrolledUp = false;
      }
    });
  }

  private saveScroll() {
    if (!this.messageContainer || !this.currentSession) return;
    const el = this.messageContainer.nativeElement;
    const key = `chatbot_scroll_${this.currentSession.id}`;
    localStorage.setItem(key, String(el.scrollTop));
  }

  private getSavedScroll(): number {
    if (!this.currentSession) return 0;
    const key = `chatbot_scroll_${this.currentSession.id}`;
    const val = localStorage.getItem(key);
    return val ? Number(val) : 0;
  }

  onScroll() {
    if (!this.messageContainer) return;
    const el = this.messageContainer.nativeElement;
    const threshold = 30;
    this.userScrolledUp = el.scrollHeight - el.scrollTop - el.clientHeight > threshold;
  }

  ngOnInit() {
    const savedId = localStorage.getItem('chatbot_sessionId');
    if (savedId) {
      this.currentSession = { id: Number(savedId) } as ChatbotSession;
    }
    this.loadSessions();
  }

  toggle() {
    if (this.isOpen) {
      this.saveScroll();
      this.isOpen = false;
      return;
    }
    this.isOpen = true;
    if (this.currentSession && this.messages.length === 0) {
      this.messages = [];
      this.isLoadingMessages = true;
      this.loadMessages();
    } else if (!this.currentSession && this.messages.length === 0) {
      this.messages.push({
        id: 0,
        sessionId: 0,
        role: 'BOT',
        content: 'Bonjour ! Je suis l\'assistant Okane. Comment puis-je vous aider ?',
        createdAt: new Date().toISOString()
      });
    }
    this.cdr.detectChanges();
    this.scrollToBottom();
  }

  setHistoryFilter(filter: string) {
    this.activeHistoryFilter = filter;
    this.cdr.detectChanges();
  }

  sendMessage() {
    const text = this.newMessage.trim();
    if (!text || this.isLoading) return;
    this.newMessage = '';
    this.isLoading = true;
    this.isTyping = true;
    this.quickReplies = [];

    this.messages.push({
      id: 0,
      sessionId: this.currentSession?.id || 0,
      role: 'USER',
      content: text,
      createdAt: new Date().toISOString()
    });
    this.scrollToBottom();

    this.chatbotService.sendMessage({
      sessionId: this.currentSession?.id,
      message: text
    }).subscribe({
      next: (response: ChatbotResponse) => {
        this.isTyping = false;
        this.isLoading = false;
        this.waitingForAgent = response.escalated;
        this.currentSession = { id: response.sessionId, title: response.sessionTitle } as ChatbotSession;
        localStorage.setItem('chatbot_sessionId', String(response.sessionId));

        this.messages.push(response.message);
        this.quickReplies = response.quickReplies || [];
        this.loadSessions();
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: () => {
        this.isTyping = false;
        this.isLoading = false;
        this.messages.push({
          id: 0,
          sessionId: this.currentSession?.id || 0,
          role: 'BOT',
          content: 'Une erreur est survenue. Veuillez réessayer.',
          createdAt: new Date().toISOString()
        });
        this.cdr.detectChanges();
      }
    });
  }

  escalateToAgent() {
    if (!this.currentSession || this.waitingForAgent || this.isLoading) return;
    this.isLoading = true;
    this.chatbotService.escalateSession(this.currentSession.id).subscribe({
      next: (session) => {
        this.waitingForAgent = true;
        this.isLoading = false;
        this.currentSession = session;
        this.messages.push({
          id: 0,
          sessionId: session.id,
          role: 'ESCALATED',
          content: 'Votre demande a été transmise à un agent humain. Un agent vous contactera sous peu.',
          createdAt: new Date().toISOString()
        });
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  sendQuickReply(text: string) {
    this.newMessage = text;
    this.sendMessage();
  }

  newSession() {
    this.currentSession = undefined;
    this.messages = [];
    this.quickReplies = [];
    this.waitingForAgent = false;
    this.isLoadingMessages = false;
    this.userScrolledUp = false;
    localStorage.removeItem('chatbot_sessionId');
    this.showHistory = false;
    this.cdr.detectChanges();
  }

  selectSession(session: ChatbotSession) {
    this.saveScroll();
    this.currentSession = session;
    this.showHistory = false;
    this.userScrolledUp = false;
    this.messages = [];
    this.isLoadingMessages = true;
    localStorage.setItem('chatbot_sessionId', String(session.id));
    this.loadMessages();
    this.cdr.detectChanges();
  }

  archiveSession(session: ChatbotSession) {
    this.chatbotService.archiveSession(session.id).subscribe({
      next: () => {
        this.loadSessions();
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('Erreur archivage session', session.id);
      }
    });
  }

  restoreSession(session: ChatbotSession) {
    this.chatbotService.restoreSession(session.id).subscribe({
      next: () => {
        this.loadSessions();
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('Erreur restauration session', session.id);
      }
    });
  }

  deleteSession(session: ChatbotSession) {
    if (confirm('Supprimer cette conversation ?')) {
      this.chatbotService.deleteSession(session.id).subscribe({
        next: () => {
          this.loadSessions();
          this.cdr.detectChanges();
        },
        error: () => {
          console.error('Erreur suppression session', session.id);
        }
      });
    }
  }

  updateTitle(event: { session: ChatbotSession; title: string }) {
    this.chatbotService.updateTitle(event.session.id, event.title).subscribe({
      next: () => {
        this.loadSessions();
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('Erreur mise à jour titre', event.session.id);
      }
    });
  }

  loadMessages() {
    if (!this.currentSession) return;
    this.chatbotService.getSessionMessages(this.currentSession.id).subscribe({
      next: (page) => {
        this.messages = page.content || page;
        this.isLoadingMessages = false;
        this.waitingForAgent = this.messages.some(m => m.role === 'ESCALATED');
        this.cdr.detectChanges();
        this.scrollToBottom();
      },
      error: () => {
        this.isLoadingMessages = false;
        this.messages = [];
        this.cdr.detectChanges();
      }
    });
  }

  loadSessions() {
    this.chatbotService.getSessions().subscribe(sessions => {
      this.allSessions = sessions;
      if (this.currentSession) {
        const found = sessions.find(s => s.id === this.currentSession!.id);
        if (found) {
          this.currentSession = found;
        }
      }
      this.cdr.detectChanges();
    });
  }
}
