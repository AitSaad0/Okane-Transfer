import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // ── Auth Layout ───────────────────────────────────────────────────────
  {
    path: '',
    loadComponent: () =>
      import('./layouts/auth-layout/auth-layout.component').then((m) => m.AuthLayoutComponent),
    children: [
      { path: '',          redirectTo: 'login', pathMatch: 'full' },
      { path: 'login',     data: { illustration: 'login' }, loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
      { path: 'register',  data: { illustration: 'register' }, loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
      { path: 'forgot-password', data: { illustration: 'forgot' }, loadComponent: () => import('./pages/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
      { path: 'reset-password',  loadComponent: () => import('./pages/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
      { path: '2fa/verify', loadComponent: () => import('./pages/two-fa-verify/two-fa-verify.component').then(m => m.TwoFaVerifyComponent) },
    ]
  },

  // ── Dashboard Layout ──────────────────────────────────────────────────
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/dashboard-layout/dashboard-layout.component').then(
        (m) => m.DashboardLayoutComponent,
      ),
    children: [
      {
        path: 'profile/security',
        loadComponent: () =>
          import('./pages/profile-security/profile-security.component').then(
            (m) => m.ProfileSecurityComponent,
          ),
      },{
        path: 'notifications',
        loadComponent: () =>
          import('./pages/shared/notification/notifications.component').then(
            (m) => m.NotificationsComponent,
          ),
      },

      // ── Admin routes ──────────────────────────────────────────────────
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        children: [
          {
            path: 'reports',
            loadComponent: () =>
              import('./pages/admin/reports/report.component').then((m) => m.ReportComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN', 'MANAGER'] },
          },
          {
            path: 'compliance/dashboard',
            loadComponent: () =>
              import('./pages/admin/compliance/compliance-dashboard/compliance-dashboard.component')
                .then(m => m.ComplianceDashboardComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'compliance/sar',
            loadComponent: () =>
              import('./pages/admin/compliance/compliance-sar/compliance-sar.component')
                .then(m => m.ComplianceSarComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'compliance/threshold',
            loadComponent: () =>
              import('./pages/admin/compliance/compliance-threshold/compliance-threshold.component')
                .then(m => m.ComplianceThresholdsComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'kyc/watchlist',
            loadComponent: () =>
              import('./pages/admin/kyc/watchlist/admin-watchlist.component')
                .then(m => m.AdminWatchlistComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'audit-logs',
            loadComponent: () =>
              import('./pages/admin/audit-logs/audit-log-list.component')
                .then(m => m.AuditLogListComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          {
            path: 'notif-broadcast',
            loadComponent: () =>
              import('./pages/admin/notifications-broadcast/notifications-broadcast.component')
                .then(m => m.BroadcastNotificationComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
          },
          // ── Users ─────────────────────────────────────────────────────
          {
            path: 'users',
            children: [
              {
                path: '',
                loadComponent: () =>
                  import('./pages/admin/users/user-list/user-list.component').then(
                    (m) => m.UserListComponent,
                  ),
              },
              {
                path: 'create',
                loadComponent: () =>
                  import('./pages/admin/users/user-form/user-form.component').then(
                    (m) => m.UserFormComponent,
                  ),
              },
              {
                path: ':id/edit',
                loadComponent: () =>
                  import('./pages/admin/users/user-form/user-form.component').then(
                    (m) => m.UserFormComponent,
                  ),
              },
              {
                path: ':id/detail',
                loadComponent: () =>
                  import('./pages/admin/users/user-detail/user-detail.component').then(
                    (m) => m.UserDetailComponent,
                  ),
              },
            ],
          },
          // ── Currencies ────────────────────────────────────────────────
          {
            path: 'currencies',
            loadComponent: () =>
              import('./pages/admin/currencies/currencies.component').then(m => m.CurrenciesComponent),
          },
          {
            path: 'currencies/create',
            loadComponent: () =>
              import('./pages/admin/currencies/currencies-create.component').then(m => m.CurrenciesCreateComponent),
          },
          // ── Corridors ─────────────────────────────────────────────────
          {
            path: 'corridors',
            loadComponent: () =>
              import('./pages/admin/corridors/corridors.component').then(m => m.CorridorsComponent),
          },
          {
            path: 'corridors/create',
            loadComponent: () =>
              import('./pages/admin/corridors/corridors-create.component').then(m => m.CorridorsCreateComponent),
          },
          // ── Exchange Rates ────────────────────────────────────────────
          {
            path: 'exchange-rates',
            loadComponent: () =>
              import('./pages/admin/exchange-rates/exchange-rates.component').then(m => m.ExchangeRatesComponent),
          },
          {
            path: 'exchange-rates/history',
            loadComponent: () =>
              import('./pages/admin/exchange-rates/exchange-rates-history.component').then(m => m.ExchangeRatesHistoryComponent),
          },
          // ── Fee Grids ─────────────────────────────────────────────────
          {
            path: 'fee-grids',
            loadComponent: () =>
              import('./pages/admin/fee-grids/fee-grids.component').then(m => m.FeeGridsComponent),
          },
          {
            path: 'fee-grids/create',
            loadComponent: () =>
              import('./pages/admin/fee-grids/fee-grids-create.component').then(m => m.FeeGridsCreateComponent),
          },
          {
            path: 'currencies',
            loadComponent: () =>
              import('./pages/admin/currencies/currencies.component').then(
                (m) => m.CurrenciesComponent
              ),
          },
          {
            path: 'currencies/create',
            loadComponent: () =>
              import('./pages/admin/currencies/currencies-create.component').then(
                (m) => m.CurrenciesCreateComponent
              ),
          },

          // ── Corridors ─────────────────────────────────────────────────
          {
            path: 'corridors',
            loadComponent: () =>
              import('./pages/admin/corridors/corridors.component').then(
                (m) => m.CorridorsComponent
              ),
          },
          {
            path: 'corridors/create',
            loadComponent: () =>
              import('./pages/admin/corridors/corridors-create.component').then(
                (m) => m.CorridorsCreateComponent
              ),
          },

          // ── Exchange Rates ────────────────────────────────────────────
          {
            path: 'exchange-rates',
            loadComponent: () =>
              import('./pages/admin/exchange-rates/exchange-rates.component').then(
                (m) => m.ExchangeRatesComponent
              ),
          },
          {
            path: 'exchange-rates/history',
            loadComponent: () =>
              import(
                './pages/admin/exchange-rates/exchange-rates-history.component'
                ).then((m) => m.ExchangeRatesHistoryComponent),
          },

          // ── Fee Grids ─────────────────────────────────────────────────
          {
            path: 'fee-grids',
            loadComponent: () =>
              import('./pages/admin/fee-grids/fee-grids.component').then(
                (m) => m.FeeGridsComponent
              ),
          },
          {
            path: 'fee-grids/create',
            loadComponent: () =>
              import('./pages/admin/fee-grids/fee-grids-create.component').then(
                (m) => m.FeeGridsCreateComponent
              ),
          },
        ],
      },

      // ── Manager routes ────────────────────────────────────────────────
      {
        path: 'manager',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
        children: [],
      },

      // ── Agent routes ──────────────────────────────────────────────────
      {
        path: 'agent',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'AGENT'] },
        children: [
          {
            path: 'transfers/new',
            loadComponent: () =>
              import('./pages/agent/transfer-creation/transfer-creation.component')
                .then(m => m.TransferCreationComponent)
          },
          {
            path: 'transfers/mobile/new',
            loadComponent: () =>
              import('./pages/agent/mobile-transfer-creation/mobile-transfer-creation.component')
                .then(m => m.MobileTransferCreationComponent)
          },
          {
            path: 'transfers/payment',
            loadComponent: () =>
              import('./pages/agent/transfer-payment/transfer-payment.component')
                .then(m => m.TransferPaymentComponent)
          }
        ]
      },

      // ── Client routes ─────────────────────────────────────────────────
      {
        path: 'client',
        canActivate: [roleGuard],
        data: { roles: ['CLIENT'] },
        children: [{
          path: 'notifications/preferences',
          loadComponent: () =>
            import('./pages/client/notifications-preferences/notifications-preferences.component')
              .then(m => m.NotificationPreferencesComponent)
        }],
      },
    ],
  },

  // ── Public error pages ────────────────────────────────────────────────
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./pages/unauthorized/unauthorized.component').then((m) => m.UnauthorizedComponent),
  },
  {
    path: 'not-found',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
  { path: '**', redirectTo: 'not-found' },
];
