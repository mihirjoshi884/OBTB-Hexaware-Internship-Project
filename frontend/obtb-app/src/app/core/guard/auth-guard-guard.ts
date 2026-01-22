import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { map, take } from 'rxjs';
import { AuthService } from '../services/Auth-Services/auth-service';

export const authGuard: CanActivateFn = (route:ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. CHECK FOR BYPASS: If there is a 'token' in the URL, allow access to Change Password
  const hasRecoveryToken = route.queryParamMap.has('token');
  const isChangePasswordPath = route.routeConfig?.path === 'change-password';

  if (hasRecoveryToken && isChangePasswordPath) {
    return true; // Bypass authentication check for password recovery
  }
  // We return the Observable directly. 
  // Angular's Router is smart enough to wait for it to emit.
  return authService.isAuthenticated$.pipe(
    // 1. Ensure the guard completes after the first 'isLoaded' emission
    take(1), 
    
    map((isLoggedIn: boolean) => {
      if (isLoggedIn) {
        // 2. Access Granted
        return true; 
      }

      // 3. Access Denied: Return a UrlTree to redirect to login
      // We pass the current URL as a query param so we can return here later
      return router.createUrlTree(['/login'], { 
        queryParams: { returnUrl: state.url } 
      });
    })
  );
};