import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [

  // ── Auth Layout ───────────────────────────────────────────────────────
  {
    path: '',
    loadComponent: () =>
      import('./layouts/auth-layout/auth-layout.component')
        .then(m => m.AuthLayoutComponent),
    children: [
      { path: '',          redirectTo: 'login', pathMatch: 'full' },
      { path: 'login',     loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
      { path: 'register',  loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
      { path: 'forgot-password', loadComponent: () => import('./pages/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
      { path: 'reset-password',  loadComponent: () => import('./pages/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
      { path: '2fa/verify', loadComponent: () => import('./pages/two-fa-verify/two-fa-verify.component').then(m => m.TwoFaVerifyComponent) },
    ]
  },

  // ── Dashboard Layout ──────────────────────────────────────────────────
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layouts/dashboard-layout/dashboard-layout.component')
        .then(m => m.DashboardLayoutComponent),
    children: [
      {
        path: 'profile/security',
        loadComponent: () => import('./pages/profile-security/profile-security.component').then(m => m.ProfileSecurityComponent)
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
              import('./pages/admin/reports/report.component')
                .then(m => m.ReportComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN', 'MANAGER'] }
          },
          {
            path: 'alerts',
            loadComponent: () =>
              import('./pages/admin/alerts/alerts.component')
                .then(m => m.AlertsComponent),
            canActivate: [roleGuard],
            data: { roles: ['ADMIN'] }
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
          }

        ]

      },

      // ── Manager routes ────────────────────────────────────────────────
      {
        path: 'manager',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER'] },
        children: []
      },

      // ── Agent routes ──────────────────────────────────────────────────
      {
        path: 'agent',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'AGENT'] },
        children: [
          {
            path: 'transfers/new',
            loadComponent: () => import('./pages/agent/transfer-creation/transfer-creation.component')
              .then(m => m.TransferCreationComponent)
          },
          {
            path: 'transfers/mobile/new',
            loadComponent: () => import('./pages/agent/mobile-transfer-creation/mobile-transfer-creation.component')
              .then(m => m.MobileTransferCreationComponent)
          },
          {
            path: 'transfers/payment',
            loadComponent: () => import('./pages/agent/transfer-payment/transfer-payment.component')
              .then(m => m.TransferPaymentComponent)
          }
        ]
      },

      // ── Client routes ─────────────────────────────────────────────────
      {
        path: 'client',
        canActivate: [roleGuard],
        data: { roles: ['CLIENT'] },
        children: []
      },
    ]
  },

  // ── Public error pages ────────────────────────────────────────────────
  { path: 'unauthorized', loadComponent: () => import('./pages/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },
  { path: 'not-found',    loadComponent: () => import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent) },
  { path: '**',           redirectTo: 'not-found' }
];
