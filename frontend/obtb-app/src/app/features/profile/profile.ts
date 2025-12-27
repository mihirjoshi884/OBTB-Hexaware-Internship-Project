import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/Auth-Services/auth-service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-linear-to-br from-gray-900 to-black pt-24 pb-10">
      <div class="max-w-2xl mx-auto px-6">
        <!-- Header -->
        <div class="bg-[#0c1222]/80 backdrop-blur-xl border border-gray-700 rounded-lg p-6 mb-6">
          <h1 class="text-3xl font-bold text-white mb-2">User Profile</h1>
          <p class="text-gray-400">Manage your account information</p>
        </div>

        <!-- Profile Card -->
        <div class="bg-[#0c1222]/80 backdrop-blur-xl border border-gray-700 rounded-lg p-8">
          <!-- Avatar Section -->
          <div class="flex items-center gap-4 mb-8">
            <div class="w-20 h-20 rounded-full bg-linear-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white text-3xl font-bold">
              {{ (username || 'U').charAt(0).toUpperCase() }}
            </div>
            <div>
              <h2 class="text-2xl font-bold text-white">{{ username }}</h2>
              <p class="text-gray-400">Account verified</p>
            </div>
          </div>

          <!-- User Info -->
          <div class="space-y-4 mb-8">
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-800/50 p-4 rounded-lg">
                <p class="text-gray-400 text-sm">Username</p>
                <p class="text-white font-semibold">{{ username }}</p>
              </div>
              <div class="bg-gray-800/50 p-4 rounded-lg">
                <p class="text-gray-400 text-sm">Account Status</p>
                <p class="text-green-400 font-semibold">Active</p>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-3">
            <button
              type="button"
              (click)="goToChangePassword()"
              class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
              Change Password
            </button>
            <button
              type="button"
              (click)="goBack()"
              class="flex-1 px-4 py-2 border border-gray-600 text-white rounded-lg hover:bg-gray-700/30 transition">
              Go Back
            </button>
          </div>
        </div>

        <!-- Additional Info -->
        <div class="mt-6 bg-[#0c1222]/80 backdrop-blur-xl border border-gray-700 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-white mb-4">Profile Information</h3>
          <p class="text-gray-400">
            This page will display more detailed profile information including email, phone number, 
            booking history, and preferences in future updates.
          </p>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ProfileComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  
  username: string = '';

  ngOnInit(): void {
    // Get username from auth service
    this.username = this.authService.getUsername();
    console.log('📄 Profile component initialized for user:', this.username);
    
    // If not logged in, redirect to login
    if (!this.authService.isLoggedIn()) {
      console.warn('⚠️ User is not logged in, redirecting to login');
      this.router.navigate(['/login']);
    }
  }

  goToChangePassword(): void {
    this.router.navigate(['/change-password']);
  }

  goBack(): void {
    this.router.navigate(['/search-bus']);
  }
}
