import { Injectable, inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { Observable, Subject, from, of } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { CurrentUserService } from './current-user.service';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private readonly router = inject(Router);
  private readonly currentUserService = inject(CurrentUserService);
  tokenExpired$: Subject<boolean> = new Subject<boolean>(); 

  /**
   * Initiates the OAuth2 Authorization Code flow with PKCE
   */
  login(): void {
    try {
      console.log('🔐 Starting OAuth2 login flow...');
      // The initCodeFlow() method handles PKCE automatically
      // since disablePKCE is set to false in authConfig
      this.oauthService.initCodeFlow();
    } catch (err) {
      console.error('❌ AuthService.login() error:', err);
      // Fallback to basic login page
      this.router.navigate(['/login']);
    }
  }

  public isReady$ = toObservable(this.currentUserService.isLoading).pipe(
    filter(isLoading => !isLoading)
  );

  // Correct: converting the Signal
  private readonly isLoaded$ = toObservable(this.currentUserService.isLoading).pipe(
    filter(loading => !loading)
  );

  // Correct: It's already an observable, just pipe directly!
  public isAuthenticated$ = this.isLoaded$.pipe(
    map(() => this.isLoggedIn()) 
  );
  /**
   * Checks if user has a valid access token
   */
  isLoggedIn(): boolean {
    try {
      const hasToken = !!this.oauthService.hasValidAccessToken();
      if (hasToken) {
        console.log('✅ User is logged in');
      }
      return hasToken;
    } catch (err) {
      console.warn('⚠️ Error checking login status:', err);
      return false;
    }
  }

  /**
   * Returns the current user's username from OIDC claims
   */
  getUsername(): string {
    try {
      const claims = this.oauthService.getIdentityClaims() as any;
      const username = claims?.preferred_username || claims?.username || claims?.name || '';
      if (username) {
        console.log('👤 Current user:', username);
      }
      return username;
    } catch (err) {
      console.warn('⚠️ Error getting username:', err);
      return '';
    }
  }

  /**
   * Returns the current access token
   */
  getAccessToken(): string {
    try {
      return this.oauthService.getAccessToken() || '';
    } catch (err) {
      console.warn('⚠️ Error getting access token:', err);
      return '';
    }
  }

  /**
   * Logs out the user by revoking token and redirecting
   */
  async logout(): Promise<void> {
    try {
      console.log('🚪 Logging out user...');
      
      // Clear current user service
      this.currentUserService.clearUser();
      
      // Clear all OAuth tokens and claims
      this.oauthService.logOut(true);
      
      // Clear session storage
      sessionStorage.clear();
      
      // Clear local storage
      localStorage.clear();
      
      console.log('📤 Tokens and session cleared');
      
      // Navigate to login
      await this.router.navigateByUrl('/login');
      
      console.log('✅ Logout complete');
    } catch (err) {
      console.error('❌ AuthService.logout error:', err);
      // Force redirect to login
      this.currentUserService.clearUser();
      sessionStorage.clear();
      localStorage.clear();
      (globalThis as any).location.href = '/login';
    }
  }

  /**
   * Refreshes the access token if it's expired
   * Uses a Promise-based approach to handle concurrent refresh requests
   */
  private refreshPromise: Promise<boolean> | null = null;

  refreshToken(): Promise<boolean> {
    if (this.refreshPromise) {
        return this.refreshPromise;
    }

    this.refreshPromise = new Promise((resolve) => {
      console.log('🔄 Attempting to refresh token...');
        this.oauthService.refreshToken()
            .then(() => {
                console.log('✅ Token refreshed successfully');
                this.refreshPromise = null; 
                resolve(true);
            })
            .catch((err) => {
                console.warn('⚠️ Token refresh failed:', err);
                this.refreshPromise = null;
                resolve(false);
            });
          });
    return this.refreshPromise;
  }

  /**
   * Observable-based refresh token method for use with RxJS operators
   * Handles concurrent refresh attempts safely
   */
  refreshTokenSafely(): Observable<boolean> {
    return from(this.refreshToken()).pipe(
      catchError((err) => {
        console.error('❌ Error during token refresh:', err);
        return of(false);
      })
    );
  }
}

