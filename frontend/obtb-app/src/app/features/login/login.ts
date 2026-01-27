import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // Added ActivatedRoute
import { OAuthService } from 'angular-oauth2-oidc';
import { environment } from '../../../environments/environment';
import { DataStore } from '../../core/data-store/data-store';
import { AuthService } from '../../core/services/auth-service'; // Added AuthService import


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  returnUrl: string = '/';
  loginForm: FormGroup;
  isPasswordVisible = false;
  errorMessage = '';

  // Cleaned up injections (removed duplicates)
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dataStore = inject(DataStore);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly oauthService = inject(OAuthService);
  private readonly authService = inject(AuthService);

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit() {
    // Capture the 'memory' from the URL query params
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/home';
  }

  togglePasswordVisibility(): void {
    this.isPasswordVisible = !this.isPasswordVisible;
  }

  goToSignup(): void {
    this.router.navigate(['/signup']);
  }

  submitLogin(): void {
    if (this.loginForm.valid) {
      this.dataStore.setLoading(true);
      this.errorMessage = ''; // Clear previous errors
      
      const { username, password } = this.loginForm.value;

      const payload = new URLSearchParams();
      payload.set('username', username);
      payload.set('password', password);

      const base = environment.baseUrls['authservice.base-uri'];
      if (!base) {
        this.dataStore.setLoading(false);
        this.errorMessage = 'Authentication service configuration not found.';
        return;
      }

      const loginUrl = `${base}/login`;
      const headers = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');

      this.http.post(loginUrl, payload.toString(), {
        headers,
        withCredentials: true 
      }).subscribe({
        next: () => {
          console.log('✅ Session established. Storing returnUrl and initiating OIDC...');
          
          // CRITICAL: Save returnUrl before the page redirects to the OIDC provider
          sessionStorage.setItem('auth_return_url', this.returnUrl);
          
          this.oauthService.initCodeFlow();
        },
        error: (err) => {
          console.error('❌ Login failed:', err);
          const serverMsg = err?.error?.error || err?.error?.message || 'Invalid credentials.';
          
          this.dataStore.setLoading(false);
          this.errorMessage = serverMsg;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.errorMessage = 'Please fill in all fields correctly';
    }
  }
}