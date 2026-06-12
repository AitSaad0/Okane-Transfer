import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-chatbot-session-empty',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="flex flex-col items-center justify-center h-full text-center p-8">
      <div class="text-6xl mb-4">💬</div>
      <h3 class="text-lg font-semibold text-slate-700 mb-2">{{ 'CHATBOT.EMPTY_TITLE' | translate }}</h3>
      <p class="text-sm text-slate-500 mb-6">{{ 'CHATBOT.EMPTY_SUBTITLE' | translate }}</p>
      <button
        (click)="onStart.emit()"
        class="bg-primary text-white px-6 py-2 rounded-lg hover:bg-primary-dark transition-colors"
      >
        {{ 'CHATBOT.EMPTY_START' | translate }}
      </button>
    </div>
  `
})
export class ChatbotSessionEmptyComponent {
  @Output() onStart = new EventEmitter<void>();
}
