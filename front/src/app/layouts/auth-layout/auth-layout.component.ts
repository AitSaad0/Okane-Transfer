import { Component, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.css'
})
export class AuthLayoutComponent {
  currentLang: string;
  open = false;
  langs = [
    { code: 'fr', label: 'Français' },
    { code: 'en', label: 'English' },
    { code: 'ar', label: 'العربية' }
  ];

  constructor(
    private translate: TranslateService,
    private tokenService: TokenService
  ) {
    this.currentLang = this.tokenService.getLang();
  }

  toggle(): void {
    this.open = !this.open;
  }

  switchLang(lang: string): void {
    this.currentLang = lang;
    this.translate.use(lang);
    this.tokenService.setLang(lang);
    document.documentElement.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
    this.open = false;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('#lang-switcher')) {
      this.open = false;
    }
  }
}
