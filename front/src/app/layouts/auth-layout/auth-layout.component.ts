import { Component, HostListener } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd, ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { TranslateModule } from '@ngx-translate/core';
import { TokenService } from '../../core/services/token.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet, TranslateModule],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.css'
})
export class AuthLayoutComponent {
  currentLang: string;
  open = false;
  illustration: string = 'login';
  langs = [
    { code: 'fr', label: 'Français' },
    { code: 'en', label: 'English' },
    { code: 'ar', label: 'العربية' }
  ];

  constructor(
    private translate: TranslateService,
    private tokenService: TokenService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.currentLang = this.tokenService.getLang();
    this.updateIllustration();
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe(() => this.updateIllustration());
  }

  private updateIllustration(): void {
    let child = this.route.snapshot.firstChild;
    while (child?.firstChild) {
      child = child.firstChild;
    }
    this.illustration = child?.data?.['illustration'] ?? 'login';
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
