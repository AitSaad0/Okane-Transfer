import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { ChatbotSession } from '../../models/chatbot.model';

@Component({
  selector: 'app-chatbot-session-item',
  standalone: true,
  imports: [CommonModule, TranslateModule, FormsModule],
  template: `
    <div
      class="flex items-center justify-between px-4 py-3 hover:bg-slate-50 cursor-pointer border-b border-slate-100 transition-colors"
      (click)="selected.emit(session)"
    >
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2">
          <span
            class="w-2 h-2 rounded-full flex-shrink-0"
            [class.bg-green-500]="session.status === 'ACTIVE'"
            [class.bg-orange-500]="session.status === 'WAITING_AGENT'"
            [class.bg-slate-300]="session.status === 'ARCHIVED'"
          ></span>
          <span *ngIf="!editing" class="text-sm font-medium text-slate-700 truncate">{{ session.title }}</span>
          <input
            *ngIf="editing"
            [(ngModel)]="editTitle"
            (keyup.enter)="saveTitle()"
            (keyup.escape)="cancelEdit()"
            (click)="$event.stopPropagation()"
            class="text-sm border border-slate-300 rounded px-2 py-1 w-full"
            autofocus
          />
        </div>
        <div class="text-xs text-slate-400 mt-1">{{ formatDate(session.updatedAt) }}</div>
      </div>
      <div class="flex items-center gap-1 ml-2" (click)="$event.stopPropagation()">
        <button
          *ngIf="session.status !== 'ARCHIVED'"
          (click)="startEdit()"
          class="p-1 text-slate-400 hover:text-slate-600 transition-colors"
          title="{{ 'CHATBOT.RENAME' | translate }}"
        >✏️</button>
        <button
          (click)="session.status !== 'ARCHIVED' ? archived.emit(session) : restored.emit(session)"
          class="p-1 text-slate-400 hover:text-slate-600 transition-colors"
          [title]="(session.status !== 'ARCHIVED' ? 'CHATBOT.ARCHIVE' : 'CHATBOT.RESTORE') | translate"
        >📦</button>
        <button
          (click)="deleted.emit(session)"
          class="p-1 text-slate-400 hover:text-red-500 transition-colors"
          title="{{ 'CHATBOT.DELETE' | translate }}"
        >🗑️</button>
      </div>
    </div>
  `
})
export class ChatbotSessionItemComponent {
  @Input() session!: ChatbotSession;
  @Output() selected = new EventEmitter<ChatbotSession>();
  @Output() archived = new EventEmitter<ChatbotSession>();
  @Output() restored = new EventEmitter<ChatbotSession>();
  @Output() deleted = new EventEmitter<ChatbotSession>();
  @Output() titleChanged = new EventEmitter<{ session: ChatbotSession; title: string }>();

  editing = false;
  editTitle = '';

  startEdit() {
    this.editing = true;
    this.editTitle = this.session.title;
  }

  saveTitle() {
    if (this.editTitle.trim() && this.editTitle !== this.session.title) {
      this.titleChanged.emit({ session: this.session, title: this.editTitle.trim() });
    }
    this.editing = false;
  }

  cancelEdit() {
    this.editing = false;
  }

  formatDate(value: any): string {
    if (!value) return '';
    if (Array.isArray(value)) {
      const [y, m, d, h, mi, s] = value;
      const dt = new Date(y, m - 1, d, h, mi, s);
      return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
    if (typeof value === 'string') {
      const dt = new Date(value);
      return isNaN(dt.getTime()) ? value : dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
    return String(value);
  }
}
