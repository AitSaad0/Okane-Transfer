import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ChatbotSession } from '../../models/chatbot.model';
import { ChatbotSessionItemComponent } from '../chatbot-session-item/chatbot-session-item.component';
import { ChatbotSessionEmptyComponent } from '../chatbot-session-empty/chatbot-session-empty.component';
import { ChatbotService } from '../../services/chatbot.service';

@Component({
  selector: 'app-chatbot-session-list',
  standalone: true,
  imports: [CommonModule, TranslateModule, ChatbotSessionItemComponent, ChatbotSessionEmptyComponent],
  template: `
    <div
      class="bg-white flex flex-col overflow-hidden h-full"
      [class.fixed]="!embedded"
      [class.right-0]="!embedded"
      [class.top-0]="!embedded"
      [class.h-full]="!embedded"
      [class.w-80]="!embedded"
      [class.shadow-2xl]="!embedded"
      [class.z-50]="!embedded"
      [class.border-l]="embedded"
      [class.border-slate-200]="embedded"
      [class.w-72]="embedded"
      [class.rounded-r-2xl]="embedded"
      [class.shadow-none]="embedded"
    >
      <div class="flex items-center justify-between px-4 py-3 border-b border-slate-200">
        <h2 class="text-lg font-semibold text-slate-800">{{ 'CHATBOT.SESSION_LIST_TITLE' | translate }}</h2>
        <div class="flex items-center gap-2">
          <button
            (click)="onNewSession.emit()"
            class="text-xs bg-primary text-white px-2 py-1 rounded hover:bg-primary-dark transition-colors"
          >+ {{ 'CHATBOT.NEW_CHAT' | translate }}</button>
          <button (click)="onClose.emit()" class="text-slate-400 hover:text-slate-600 text-xl">&times;</button>
        </div>
      </div>

      <div class="flex border-b border-slate-200">
        <button
          *ngFor="let f of filters"
          (click)="setFilter(f.key)"
          class="flex-1 py-2 text-sm font-medium transition-colors"
          [class.text-primary]="activeFilter === f.key"
          [class.border-b-2]="activeFilter === f.key"
          [class.border-primary]="activeFilter === f.key"
          [class.text-slate-500]="activeFilter !== f.key"
        >{{ f.label | translate }}</button>
      </div>

      <div class="flex-1 overflow-y-auto">
        <app-chatbot-session-empty
          *ngIf="sessions.length === 0"
          (onStart)="onNewSession.emit()"
        ></app-chatbot-session-empty>
        <app-chatbot-session-item
          *ngFor="let s of sessions"
          [session]="s"
          (selected)="onSelectSession.emit($event)"
          (archived)="archiveSession($event)"
          (restored)="restoreSession($event)"
          (deleted)="deleteSession($event)"
          (titleChanged)="updateTitle($event)"
        ></app-chatbot-session-item>
      </div>
    </div>
  `
})
export class ChatbotSessionListComponent implements OnInit {
  @Input() sessions: ChatbotSession[] = [];
  @Input() embedded = false;
  @Output() onSelectSession = new EventEmitter<ChatbotSession>();
  @Output() onNewSession = new EventEmitter<void>();
  @Output() onClose = new EventEmitter<void>();

  activeFilter = 'ALL';

  filters = [
    { key: 'ALL', label: 'CHATBOT.FILTER_ALL' },
    { key: 'ACTIVE', label: 'CHATBOT.FILTER_ACTIVE' },
    { key: 'ARCHIVED', label: 'CHATBOT.FILTER_ARCHIVED' }
  ];

  constructor(
    private chatbotService: ChatbotService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadSessions();
  }

  setFilter(filter: string) {
    this.activeFilter = filter;
    this.loadSessions();
    this.cdr.detectChanges();
  }

  loadSessions() {
    const status = this.activeFilter === 'ALL' ? undefined : this.activeFilter;
    this.chatbotService.getSessions(status).subscribe(sessions => {
      this.sessions = sessions;
      this.cdr.detectChanges();
    });
  }

  archiveSession(session: ChatbotSession) {
    this.chatbotService.archiveSession(session.id).subscribe(() => {
      this.loadSessions();
      this.cdr.detectChanges();
    });
  }

  restoreSession(session: ChatbotSession) {
    this.chatbotService.restoreSession(session.id).subscribe(() => {
      this.loadSessions();
      this.cdr.detectChanges();
    });
  }

  deleteSession(session: ChatbotSession) {
    if (confirm('Supprimer cette conversation ?')) {
      this.chatbotService.deleteSession(session.id).subscribe(() => {
        this.loadSessions();
        this.cdr.detectChanges();
      });
    }
  }

  updateTitle(event: { session: ChatbotSession; title: string }) {
    this.chatbotService.updateTitle(event.session.id, event.title).subscribe(() => {
      this.loadSessions();
      this.cdr.detectChanges();
    });
  }
}
