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

    // If we are NOT on the callback page, initialize normally
    if (!window.location.pathname.includes('/login/callback')) {
      this.oauthService.loadDiscoveryDocument().then(() => {
        this.oauthService.setupAutomaticSilentRefresh();
        console.log('🛡️ App: Standard initialization complete');
      });
    } else {
      console.log('🛡️ App: Callback detected, yielding to LoginCallbackComponent');
    }
  }
}