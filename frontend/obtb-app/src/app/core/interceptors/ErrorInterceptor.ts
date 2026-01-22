import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/Auth-Services/auth-service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            // Check if it's a 401 and NOT a request to the token endpoint itself
            if (error.status === 401 && !req.url.includes('/oauth2/token')) {
                console.warn('⚠️ 401 detected, attempting auto-refresh...');
                
                // Use the auth service's refresh method which handles concurrency
                return authService.refreshTokenSafely().pipe(
                    switchMap((success) => {
                        if (success) {
                            console.log('✅ Token refreshed, retrying request...');
                            // Get the new token and retry
                            const newToken = authService.getAccessToken();
                            const retryReq = req.clone({
                                setHeaders: { Authorization: `Bearer ${newToken}` }
                            });
                            return next(retryReq);
                        } else {
                            console.error('❌ Token refresh failed, logging out...');
                            authService.logout();
                            return throwError(() => error);
                        }
                    }),
                    catchError((refreshError) => {
                        console.error('❌ Error during token refresh:', refreshError);
                        authService.logout();
                        return throwError(() => error);
                    })
                );
            }
            return throwError(() => error);
        })
    );
};