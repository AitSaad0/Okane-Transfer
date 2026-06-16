import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

// Updated labels
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  isCollapsed = false;
  userRole = '';

  allNavItems: NavItem[] = [
    { label: 'NAV_USERS',               icon: 'fa-solid fa-users',                  route: '/admin/users',                   roles: ['ADMIN'] },
    { label: 'AGENCIES_NAV',                icon: 'fa-solid fa-building',               route: '/admin/agencies',                roles: ['ADMIN'] },
    { label: 'CURRENCIES.TITLE',              icon: 'fa-solid fa-coins',                  route: '/admin/currencies',              roles: ['ADMIN'] },
    { label: 'CORRIDORS',               icon: 'fa-solid fa-route',                  route: '/admin/corridors',               roles: ['ADMIN'] },
    { label: 'RATES',                   icon: 'fa-solid fa-chart-line',             route: '/admin/exchange-rates',          roles: ['ADMIN'] },
    { label: 'SIDEBAR_FEES',                    icon: 'fa-solid fa-hand-holding-dollar',    route: '/admin/fee-grids',               roles: ['ADMIN'] },
    { label: 'SIDEBAR_COMPLIANCE',              icon: 'fa-solid fa-shield-halved',          route: '/admin/compliance/dashboard',    roles: ['ADMIN'] },
    { label: 'Watchlist',               icon: 'fa-solid fa-list-check',             route: '/admin/kyc/watchlist',           roles: ['ADMIN'] },
    { label: 'AUDIT',                   icon: 'fa-solid fa-clipboard-list',         route: '/admin/audit-logs',              roles: ['ADMIN'] },
    { label: 'MOBILE_MONEY',            icon: 'fa-solid fa-mobile-screen',          route: '/admin/mobile-money',            roles: ['ADMIN'] },
    { label: 'Alerts',                  icon: 'fa-solid fa-triangle-exclamation',   route: '/admin/alerts',                  roles: ['ADMIN'] },
    { label: 'Broadcast Notifications', icon: 'fa-solid fa-satellite-dish',         route: '/admin/notif-broadcast',         roles: ['ADMIN'] },
    { label: 'TRANSFERS', icon: 'fa-solid fa-arrow-right-arrow-left', route: '/admin/transfers', roles: ['ADMIN'] },
    { label: 'REPORTS', icon: 'fa-solid fa-chart-bar', route: '/admin/reports', roles: ['ADMIN'] },
    { label: 'MGR_TRANSFERS', icon: 'fa-solid fa-arrow-right-arrow-left', route: '/manager/transfer', roles: ['MANAGER'] },
    { label: 'MGR_DASHBOARD', icon: 'fa-solid fa-gauge', route: '/manager/dashboard', roles: ['MANAGER'] },
    { label: 'MGR_REPORTS',             icon: 'fa-regular fa-file-lines',           route: '/manager/reports',               roles: ['MANAGER'] },
    { label: 'NEW_TRANSFER',            icon: 'fa-solid fa-plus',                   route: '/agent/transfers/new',           roles: ['AGENT'] },
    { label: 'WITHDRAWAL',              icon: 'fa-solid fa-money-bill-wave',        route: '/agent/transfers/payment',       roles: ['AGENT'] },
    { label: 'MOBILE_TRANSFER_SIDEBAR', icon: 'fa-solid fa-mobile-screen',          route: '/agent/transfers/mobile/new',    roles: ['AGENT'] },
    { label: 'CASH_REGISTER',           icon: 'fa-solid fa-cash-register',          route: '/agent/cash-register',           roles: ['AGENT'] },
    { label: 'CHATBOT_LABEL', icon: 'fa-solid fa-robot', route: '/client/chatbot', roles: ['CLIENT', 'AGENT'] },
    { label: 'MY_DASHBOARD',            icon: 'fa-solid fa-house',                  route: '/client/dashboard',              roles: ['CLIENT'] },
    { label: 'MY_TRANSFERS',            icon: 'fa-solid fa-paper-plane',            route: '/client/transfers',              roles: ['CLIENT'] },
    { label: 'MY_PROFILE',              icon: 'fa-regular fa-circle-user',          route: '/client/profile',                roles: ['CLIENT'] },
  ];

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loadUserRole();
  }

  private loadUserRole(): void {
    const role = this.auth.getRole();
    this.userRole = role ?? 'CLIENT';
  }

  get navItems(): NavItem[] {
    return this.allNavItems.filter(item =>
      item.roles.includes(this.userRole)
    );
  }

  toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  goHome(): void { this.router.navigate(['/login']); }
}
