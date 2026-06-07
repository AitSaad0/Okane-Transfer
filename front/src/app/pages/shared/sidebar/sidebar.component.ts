import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router,RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

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
    { label: 'USERS',         icon: '👥', route: '/admin/users',                   roles: ['ADMIN'] },
    { label: 'AGENCIES',      icon: '🏢', route: '/admin/agencies',                roles: ['ADMIN'] },
    { label: 'CURRENCIES',    icon: '💱', route: '/admin/currencies',              roles: ['ADMIN'] },
    { label: 'CORRIDORS',     icon: '🔀', route: '/admin/corridors',               roles: ['ADMIN'] },
    { label: 'RATES',         icon: '📈', route: '/admin/exchange-rates',          roles: ['ADMIN'] },
    { label: 'FEES',          icon: '💰', route: '/admin/fee-grids',               roles: ['ADMIN'] },
    { label: 'COMPLIANCE',    icon: '🛡️', route: '/admin/compliance/dashboard',    roles: ['ADMIN'] },
    { label: 'AUDIT',         icon: '📋', route: '/admin/audit-logs',              roles: ['ADMIN'] },
    { label: 'MOBILE_MONEY',  icon: '📱', route: '/admin/mobile-money',            roles: ['ADMIN'] },
    { label: 'BROADCAST',     icon: '📣', route: '/admin/notifications/broadcast', roles: ['ADMIN'] },

    { label: 'TRANSFERS',     icon: '📦', route: '/admin/transfers',               roles: ['ADMIN', 'MANAGER'] },
    { label: 'REPORTS',       icon: '📊', route: '/admin/reports',           roles: ['ADMIN', 'MANAGER'] },

    { label: 'DASHBOARD',     icon: '🖥️', route: '/manager/dashboard',            roles: ['MANAGER'] },
    { label: 'MGR_REPORTS',   icon: '📄', route: '/manager/reports',              roles: ['MANAGER'] },

    { label: 'NEW_TRANSFER',  icon: '➕', route: '/agent/transfers/new',           roles: ['AGENT'] },
    { label: 'CASH_REGISTER', icon: '🏦', route: '/agent/cash-register',           roles: ['AGENT'] },

    { label: 'CHATBOT',       icon: '🤖', route: '/client/chatbot',                roles: ['CLIENT', 'AGENT'] },
    { label: 'MY_DASHBOARD',  icon: '🏠', route: '/client/dashboard',              roles: ['CLIENT'] },
    { label: 'MY_TRANSFERS',  icon: '📤', route: '/client/transfers',              roles: ['CLIENT'] },
    { label: 'MY_PROFILE',    icon: '👤', route: '/client/profile',                roles: ['CLIENT'] },
  ];

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loadUserRole();
  }

  private loadUserRole(): void {
    const role = this.auth.getRole();

    // fallback safety (prevents empty UI issues)
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
