import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from './core/configs/auth-config';
import { Footer } from './shared/footer/footer';
import { LoaderComponent } from './shared/loader/loader';
import { Navbar } from './shared/navbar/navbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar, Footer, LoaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(private oauthService: OAuthService, private router: Router) {
    this.configureAuth();
  }

  private configureAuth() {
    this.oauthService.configure(authConfig);

    if (window.location.pathname.includes('/login/callback')) {
      // Just load the document so the service is ready for the Callback Component.
      // We do NOT call setupAutomaticSilentRefresh here because the 
      // Callback Component will do it after the tokens are actually received.
      this.oauthService.loadDiscoveryDocument().then(() => {
        console.log('🛡️ App: Discovery loaded for callback');
      }).catch((err) => {
        console.warn('⚠️ Discovery doc failed, using manual config', err);
        // Endpoints are already configured in authConfig, continue without discovery
      });
    } else {
      // Normal app boot or page refresh.
      this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
        if (this.oauthService.hasValidAccessToken()) {
          this.oauthService.setupAutomaticSilentRefresh();
          console.log('🛡️ App: Silent refresh active');
        }
      }).catch(err => {
        console.warn('⚠️ App: Discovery document failed, using manual endpoint config', err);
        // Endpoints are already in authConfig, so continue
        // Try to restore any existing session
        if (this.oauthService.hasValidAccessToken()) {
          this.oauthService.setupAutomaticSilentRefresh();
          console.log('🛡️ App: Using existing token, silent refresh active');
        }
      });
    }
  }
}