import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { AuthService } from '../../core/services/auth-service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.html',
  styleUrls: ['./change-password.css'],
})
export class ChangePasswordComponent implements OnInit {

  @Input() theme: 'light' | 'dark' = 'light';

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly authBaseUrl = environment.baseUrls['authservice.base-uri'];

  changePasswordForm!: FormGroup;
  securityQuestionsForm!: FormGroup;
  currentStep: number = 1;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  isSubmitted: boolean = false;

  // Password visibility toggles
  showCurrentPassword: boolean = false;
  showNewPassword: boolean = false;
  showConfirmPassword: boolean = false;

  // Hardcoded security questions (same as signup wizard)
  securityQuestions = [
    'What is the first phone number you memorized as a child?',
    'What is the last name of your favorite high school teacher?',
  ];

  /** * FIX: Renamed from 'securityQuestions' to 'userSecurityQuestions' 
   * to resolve the TS2551 template error.
   */
  userSecurityQuestions = [
    'What is the first phone number you memorized as a child?',
    'What is the last name of your favorite high school teacher?',
  ];
isRecoveryMode: boolean = false; // New flag

ngOnInit(): void {
  // Check if token exists in URL
  this.isRecoveryMode = !!this.route.snapshot.queryParamMap.get('token');
  this.initializeForms();
}

initializeForms(): void {
  this.changePasswordForm = this.fb.group({
    // If in recovery mode, currentPassword is NOT required
    currentPassword: ['', this.isRecoveryMode ? [] : [Validators.required, Validators.minLength(8)]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
  }, { validators: this.passwordMatchValidator });

    // Step 2: Security Questions Form
    this.securityQuestionsForm = this.fb.group({
      answer1: ['', [Validators.required, Validators.minLength(2)]],
      answer2: ['', [Validators.required, Validators.minLength(2)]],
    });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword && confirmPassword && newPassword !== confirmPassword ? { passwordMismatch: true } : null;
  }

  onProceedToSecurityQuestions(): void {
    this.errorMessage = '';
    if (this.changePasswordForm.invalid) {
      this.errorMessage = 'Please fill in all fields correctly';
      return;
    }
    if (this.changePasswordForm.hasError('passwordMismatch')) {
      this.errorMessage = 'Passwords do not match';
      return;
    }
    this.currentStep = 2;
  }

  onSubmitChangePassword(): void {
    if (this.securityQuestionsForm.invalid) {
      this.errorMessage = 'Please answer all security questions';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const emailFromUrl = this.route.snapshot.queryParamMap.get('email') || '';
    const urlToken = this.route.snapshot.queryParamMap.get('token') || '';
    const sessionToken = this.authService.getAccessToken();
    const token = urlToken || sessionToken;

    if (!token) {
      this.errorMessage = 'Session expired. Please log in again.';
      return;
    }
    // Prepare payload to match your Java Backend logic
    const requestBody = {
      email: emailFromUrl,
      currentPassword: this.changePasswordForm.value.currentPassword,
      newPassword: this.changePasswordForm.value.newPassword,
      // Sending the question text + the answer so the backend can hash them together
      securityVerification: [
        {
          question: this.userSecurityQuestions[0],
          answer: this.securityQuestionsForm.value.answer1.trim()
        },
        {
          question: this.userSecurityQuestions[1],
          answer: this.securityQuestionsForm.value.answer2.trim()
        }
      ]
    };
  

  const headers = {
    'Authorization': `Bearer ${token}`
  };

    this.http.post(`${this.authBaseUrl}/auth-api/v1/change-password`, requestBody,{ headers})
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.isSubmitted = true;
          this.successMessage = 'Password updated successfully!';
          setTimeout(() => this.router.navigate(['/profile']), 2000);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Verification failed. Please check your answers.';
        }
      });
  }

  onBackToStep1(): void {
    this.currentStep = 1;
    this.errorMessage = '';
  }

  onBackToProfile(): void {
    if (this.isRecoveryMode) {
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/profile']);
    }
  }

  togglePasswordVisibility(field: string): void {
    if (field === 'current') this.showCurrentPassword = !this.showCurrentPassword;
    else if (field === 'new') this.showNewPassword = !this.showNewPassword;
    else if (field === 'confirm') this.showConfirmPassword = !this.showConfirmPassword;
  }
}