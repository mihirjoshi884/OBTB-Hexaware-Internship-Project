import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';


@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPassword implements OnInit {

  private readonly http = inject(HttpClient);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly userBaseUrl = environment.baseUrls['userservice.base-uri'];
  private readonly authBaseUrl = environment.baseUrls['authservice.base-uri'];
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  forgotPasswordForm!: FormGroup;
  currentStep: number = 1;
  errorMessage: string = '';
  successMessage: string = '';
  recoveredEmail: string = '';
  isLoading: boolean = false;

  constructor() {}

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    this.forgotPasswordForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
    });
  }

  // Step 1: Fetch Account
  onFetchAccount(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.forgotPasswordForm.invalid) {
      this.errorMessage = 'Please enter a valid username';
      this.cdr.markForCheck();
      return;
    }

    this.isLoading = true;
    this.cdr.markForCheck();

    this.http.get(`${this.userBaseUrl}/user-api/v1/fetch-user-email/${this.forgotPasswordForm.value.username}`, 
      { observe: 'response' })
      .subscribe({
        next: (response) => {
          console.log('Full Response Object:', response);
          console.log('Response Body:', response.body);
          
          if (response.body) {
            const responseData: any = response.body;
            // The API returns email in the body property, extract it properly
            this.recoveredEmail = responseData.body || responseData.email || responseData.data || '';
            console.log('Recovered Email:', this.recoveredEmail);
            this.successMessage = responseData.message || 'Account found!';
            this.currentStep = 2;
          }
          this.isLoading = false;
          this.cdr.markForCheck(); // Force change detection
        },
        error: (error) => {
          console.error('Frontend Error:', error);
          this.errorMessage = error.error?.message || 'Error fetching account.';
          this.isLoading = false;
          this.cdr.markForCheck(); // Force change detection
        }
      });
  }  // Step 2: Send Recovery Email
  onSendRecoveryEmail(): void {
    this.errorMessage = '';
    this.isLoading = true;
    this.cdr.markForCheck();

    const requestBody = { 
    username: this.forgotPasswordForm.value.username,
    email: this.recoveredEmail 
    };
    //recovery mail api call.
    this.http.post(`${this.authBaseUrl}/auth-api/v1/recover-account`, 
      requestBody, 
      { observe: 'response'})
      .subscribe({
        next: (response) => {
          console.log('Full Response Object:', response);
          console.log('Response Body:', response.body);
          
          if (response.body) {
            const responseData: any = response.body;
            this.successMessage = responseData.message || 'Recovery email sent!';
            this.currentStep = 3;
          }
          this.isLoading = false;
          this.cdr.markForCheck(); // Force change detection
        },
        error: (error) => {
          console.error('Frontend Error:', error);
          this.errorMessage = error.error?.message || 'Error sending recovery email.';
          this.isLoading = false;
          this.cdr.markForCheck(); // Force change detection
        }
      });
  }

  // Step 3: Back to Login
  onBackToLogin(): void {
    this.router.navigate(['/login']);
  }

  // Reset form
  resetForm(): void {
    this.currentStep = 1;
    this.forgotPasswordForm.reset();
    this.errorMessage = '';
    this.successMessage = '';
    this.recoveredEmail = '';
    this.cdr.markForCheck();
  }


}
