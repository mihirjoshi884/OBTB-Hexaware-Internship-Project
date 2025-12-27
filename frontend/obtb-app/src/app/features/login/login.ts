import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';
import { DataStore } from '../../core/data-store/data-store';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  isPasswordVisible = false;
  errorMessage = '';

  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly dataStore = inject(DataStore);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly oauthService = inject(OAuthService);

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  togglePasswordVisibility(): void {
    this.isPasswordVisible = !this.isPasswordVisible;
  }

  goToSignup(): void {
    this.router.navigate(['/signup']);
  }


  submitLogin(): void {
    if (this.loginForm.valid) {
      // Show loader
      this.dataStore.setLoading(true);
      
      const { username, password } = this.loginForm.value;

      // Step 1: Authenticate with Spring Backend
      const payload = new URLSearchParams();
      payload.set('username', username);
      payload.set('password', password);

      const base = environment.baseUrls['authservice.base-uri'];
      if (!base) {
        this.dataStore.setLoading(false);
        this.errorMessage = 'Authentication service configuration not found. Please refresh the page.';
        return;
      }

      const loginUrl = base + '/login';
      const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');

      console.log('🔐 Attempting login POST:', { url: loginUrl });

      this.http.post(loginUrl, payload.toString(), {
        headers,
        withCredentials: true // Send/receive cookies (JSESSIONID)
      }).subscribe({
        next: () => {
          console.log('✅ Spring session established. Starting OIDC Flow...');
          // STEP 2: Trigger the OIDC Code Flow
          // Because 'withCredentials: true' saved the session cookie, 
          // the Auth Server will recognize the user and redirect back immediately.
          this.oauthService.initCodeFlow();
        },
        error: (err) => {
          console.error('❌ Spring authentication failed:', err.status, err.error || err.message || err);
          // Extract backend message if present
          const serverMsg = err?.error?.error || err?.error?.message || 'Invalid username or password. Please try again.';
          // Defer UI updates to next tick
          setTimeout(() => {
            this.dataStore.setLoading(false);
            this.errorMessage = serverMsg;
            this.cdr.detectChanges();
          }, 0);
        }
      });
    } else {
      this.errorMessage = 'Please fill in all fields correctly';
    }
  }
}
