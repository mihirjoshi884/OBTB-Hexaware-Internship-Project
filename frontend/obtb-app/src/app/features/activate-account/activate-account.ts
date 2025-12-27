import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DataStore } from '../../core/data-store/data-store';

@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activate-account.html',
  styleUrl: './activate-account.css',
})
export class ActivateAccount implements OnInit {
  loading = signal(false);
  success = signal(false);
  errorMessage = signal('');

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly dataStore = inject(DataStore);

  ngOnInit(): void {
    // Check if user came from verify-account component
    const userId = this.dataStore.getUserId();
    if (!userId) {
      this.errorMessage.set('User ID not found. Please verify your account first.');
      this.success.set(false);
    }
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  goLogin(): void {
    this.router.navigate(['/login']);
  }
}
