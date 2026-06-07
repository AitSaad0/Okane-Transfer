// navbar.component.ts
import { Component, OnInit } from '@angular/core';
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
  unreadNotifications = 3;

  user: any = {
    name: '',
    role: '',
    avatar: null
  };

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
        this.user = user;
      },
      error: () => {
        // fallback if API fails or token invalid
        this.user = {
          name: 'User',
          role: this.auth.getRole() || 'UNKNOWN',
          avatar: null
        };
      }
    });
  }

  switchLang(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
    this.applyDir(lang);
  }

  applyDir(lang: string) {
    document.documentElement.setAttribute(
      'dir',
      lang === 'ar' ? 'rtl' : 'ltr'
    );
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  logout() {
    this.auth.logout();
  }

  get initials(): string {
    if (!this.user?.name) return 'U';

    return this.user.name
      .split(' ')
      .map((n: string) => n[0])
      .join('')
      .toUpperCase();
  }
}
