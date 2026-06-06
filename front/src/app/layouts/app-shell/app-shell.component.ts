import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent implements OnInit {

  constructor(
    private translate: TranslateService,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    const lang = this.tokenService.getLang();
    this.translate.setDefaultLang('fr');
    this.translate.use(lang);
    document.documentElement.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
  }
}
