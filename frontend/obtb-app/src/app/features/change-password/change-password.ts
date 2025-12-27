import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/Auth-Services/auth-service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-linear-to-br from-gray-900 to-black pt-24 pb-10">
      <div class="max-w-md mx-auto px-6">
        <!-- Header -->
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-white mb-2">Change Password</h1>
          <p class="text-gray-400">Update your account password</p>
        </div>

        <!-- Change Password Form -->
        <div class="bg-[#0c1222]/80 backdrop-blur-xl border border-gray-700 rounded-lg p-8">
          @if (!isSubmitted) {
            <form (ngSubmit)="handleChangePassword()" class="space-y-4">
              <!-- Current Password -->
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Current Password
                </label>
                <input
                  type="password"
                  [(ngModel)]="currentPassword"
                  name="currentPassword"
                  placeholder="Enter your current password"
                  class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition"
                  required />
              </div>

              <!-- New Password -->
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  New Password
                </label>
                <input
                  type="password"
                  [(ngModel)]="newPassword"
                  name="newPassword"
                  placeholder="Enter your new password"
                  class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition"
                  required />
              </div>

              <!-- Confirm New Password -->
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-2">
                  Confirm New Password
                </label>
                <input
                  type="password"
                  [(ngModel)]="confirmPassword"
                  name="confirmPassword"
                  placeholder="Confirm your new password"
                  class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition"
                  required />
              </div>

              <!-- Error Message -->
              @if (errorMessage) {
                <div class="p-3 bg-red-500/10 border border-red-500 text-red-400 rounded-lg text-sm">
                  {{ errorMessage }}
                </div>
              }

              <!-- Buttons -->
              <div class="flex gap-3 pt-4">
                <button
                  type="submit"
                  class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
                  Update Password
                </button>
                <button
                  type="button"
                  (click)="goBack()"
                  class="flex-1 px-4 py-2 border border-gray-600 text-white rounded-lg hover:bg-gray-700/30 transition">
                  Cancel
                </button>
              </div>
            </form>
          } @else {
            <div class="text-center py-8">
              <div class="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
              </div>
              <h2 class="text-xl font-bold text-white mb-2">Password Updated Successfully!</h2>
              <p class="text-gray-400 mb-6">Your password has been changed successfully.</p>
              <button
                type="button"
                (click)="goToProfile()"
                class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
                Back to Profile
              </button>
            </div>
          }
        </div>

        <!-- Info -->
        <div class="mt-6 bg-[#0c1222]/80 backdrop-blur-xl border border-gray-700 rounded-lg p-6">
          <p class="text-gray-400 text-sm">
            Password change functionality will be integrated with your backend API in future updates.
            For now, this form is for demonstration purposes.
          </p>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ChangePasswordComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  errorMessage = '';
  isSubmitted = false;

  handleChangePassword(): void {
    this.errorMessage = '';

    // Validation
    if (!this.currentPassword) {
      this.errorMessage = 'Current password is required';
      return;
    }

    if (!this.newPassword || this.newPassword.length < 8) {
      this.errorMessage = 'New password must be at least 8 characters';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    // In a real app, you would call a backend API here
    console.log('Password change request:', {
      currentPassword: this.currentPassword,
      newPassword: this.newPassword
    });

    // Simulate success
    this.isSubmitted = true;
  }

  goBack(): void {
    if (this.isSubmitted) {
      this.goToProfile();
    } else {
      this.router.navigate(['/profile']);
    }
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
  }
}
