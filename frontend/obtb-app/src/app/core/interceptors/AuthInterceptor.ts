import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { AuthService } from '../services/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const oauthService = inject(OAuthService);

    const token = authService.getAccessToken(); 
    const tokenEndpoint = oauthService.tokenEndpoint;

    const isTokenRequest = tokenEndpoint && req.url.includes(tokenEndpoint);
    const isLoginRequest = req.url.includes('/login') || req.url.includes('/oauth2/authorize');

    // Only attach the token if it exists and it's not a token/login request
    if (token && !isTokenRequest && !isLoginRequest) {
        const clonedRequest = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        return next(clonedRequest);
    }

    return next(req);
};