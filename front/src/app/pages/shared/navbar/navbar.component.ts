import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  currentLang = 'fr';
  languages = ['fr', 'en', 'ar'];
  isDropdownOpen = false;
  isLangDropdownOpen = false;
  unreadNotifications = 3;
  user: any = { nom: '', prenom: '', role: '' };

  constructor(
    private translate: TranslateService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.currentLang = localStorage.getItem('lang') || 'fr';
    this.translate.use(this.currentLang);
    this.applyDir(this.currentLang);
    this.loadUser();
  }

  loadUser(): void {
    this.auth.me().subscribe({
      next: (user) => {
        // Mapper tous les cas possibles sans changer la structure
        setTimeout(() => {
          this.user = {
            prenom: user?.prenom ?? user?.firstName ?? '',
            nom:    user?.nom    ?? user?.lastName  ?? user?.name ?? '',
            role:   user?.role   ?? user?.roles?.[0] ?? '',
          };
        });
      },
      error: () => {
        setTimeout(() => {
          this.user = { nom: 'User', prenom: '', role: this.auth.getRole() || 'UNKNOWN' };
        });
      }
    });
  }

  switchLang(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
    this.applyDir(lang);
    this.isLangDropdownOpen = false;
  }

  applyDir(lang: string) {
    document.documentElement.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
    this.isLangDropdownOpen = false;
  }

  toggleLangDropdown() {
    this.isLangDropdownOpen = !this.isLangDropdownOpen;
    this.isDropdownOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.navbar__user') && !target.closest('.navbar__lang-wrapper')) {
      this.isDropdownOpen = false;
      this.isLangDropdownOpen = false;
    }
  }

  logout() { this.auth.logout(); }

  get fullName(): string {
    const prenom = this.user?.prenom || '';
    const nom = this.user?.nom || '';
    return `${prenom} ${nom}`.trim() || 'User';
  }

  get initials(): string {
    const p = this.user?.prenom?.[0] || '';
    const n = this.user?.nom?.[0] || '';
    return (p + n).toUpperCase() || 'U';
  }
}
