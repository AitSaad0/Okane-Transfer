  import { inject } from '@angular/core';
  import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
  import { TokenService } from '../services/token.service';

  export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
    const tokenService = inject(TokenService);
    const router = inject(Router);

    const requiredRoles: string[] = route.data['roles'] ?? [];
    const userRole = tokenService.getRole();

    if (!userRole) {
      router.navigate(['/login']);
      return false;
    }

    if (requiredRoles.length === 0 || requiredRoles.includes(userRole)) {
      return true;
    }

    router.navigate(['/unauthorized']);
    return false;
  };
