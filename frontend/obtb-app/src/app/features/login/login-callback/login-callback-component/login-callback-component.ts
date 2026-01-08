import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { CurrentUserService } from '../../../../core/services/Auth-Services/current-user.service';


@Component({
  selector: 'app-login-callback-component',
  templateUrl: './login-callback-component.html',
  standalone: true
})
export class LoginCallbackComponent implements OnInit {
  private readonly oauthService = inject(OAuthService);
  private readonly router = inject(Router);
  private readonly currentUserService = inject(CurrentUserService);

  ngOnInit(): void {
    console.log('📍 Login callback component initialized');
    this.oauthService.loadDiscoveryDocumentAndTryLogin().then((success) => {
    if (this.oauthService.hasValidAccessToken()) {
      console.log('✅ OIDC Token Exchange Successful');
      
      // Start the refresh timer NOW because App component skipped it
      this.oauthService.setupAutomaticSilentRefresh();
      
      const claims = this.oauthService.getIdentityClaims();
      // Initialize current user service with user data
      this.currentUserService.initializeUser(claims);
      this.notifyLoginSuccess(claims);

      const savedUrl = sessionStorage.getItem('auth_return_url');
      sessionStorage.removeItem('auth_return_url');
      
      if (savedUrl && savedUrl !== '/' && savedUrl !== '/login') {
        console.log('🔄 Guard-triggered login: returning to', savedUrl);
        this.router.navigateByUrl(savedUrl);
      } else {
        console.log('🏠 Standard login: going to default /search-bus');
        this.router.navigate(['/search-bus']);
      }
    } else {
      console.error('❌ OIDC Login Failed');
      this.router.navigate(['/login']);
    }
    }).catch(err => {
      console.error('❌ Error during discovery/exchange:', err);
      this.router.navigate(['/login']);
    });
  }

  private notifyLoginSuccess(claims: any): void {
    try {
      const username = claims?.preferred_username || claims?.username || claims?.name || 'User';
      const email = claims?.email || 'N/A';
      
      console.log('✅✅✅ LOGIN SUCCESSFUL ✅✅✅');
      console.log('');
      console.log('👤 User Information:');
      console.log('   Username:', username);
      console.log('   Email:', email);
      console.log('');
      console.log('🔐 Token Information:');
      const accessToken = this.oauthService.getAccessToken();
      console.log('   Access Token Length:', accessToken?.length || 0);
      console.log('   Token Valid Until:', this.oauthService.getAccessTokenExpiration());
      console.log('');
      
      // Log user summary from service
      this.currentUserService.logUserInfo();
    } catch (error) {
      console.error('⚠️ Error in notifyLoginSuccess:', error);
    }
  }

}
