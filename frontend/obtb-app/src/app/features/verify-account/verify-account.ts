import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { DataStore } from '../../core/data-store/data-store';

@Component({
  selector: 'app-verify-account',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verify-account.html',
})
export class VerifyAccount implements OnInit {
  loading = signal(true);
  success = signal(false);
  isAccountActive = signal(true);
  errorMessage = signal('');
  Id: string | null = null;

  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly dataStore = inject(DataStore);

  ngOnInit(): void {
    this.verifyUser();
  }

  verifyUser(): void {
    const userId = this.route.snapshot.queryParamMap.get('userId');
    if (!userId) {
      this.loading.set(false);
      this.errorMessage.set('Invalid verification link.');
      return;
    }
    this.dataStore.setUserId(userId);
    this.activeAccount(userId);

    this.http.patch(environment.baseUrls['authservice.base-uri'] + `/auth-api/v1/user/verify/${userId}`, {})
      .subscribe({
        next: (response: any) => {
          console.log('Verification successful:', response);
          this.success.set(true);
          this.loading.set(false);
        },
        error: (error: any) => {
          console.error('Verification failed:', error);
          this.success.set(false);
          this.loading.set(false);
          this.errorMessage.set(error.errorMessage);
        }
      });
  }

  retry(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.verifyUser();
  }

  activeAccount(userId: string): void {
    this.http.get<boolean>(environment.baseUrls['authservice.base-uri'] + `/auth-api/v1/user/is-active/${userId}`,{})
      .subscribe({
        next: (isActive: boolean) => {
          console.log('Is user active? ', isActive);
          this.isAccountActive.set(isActive);
        },
        error: (err: any) => {
          console.error('Error checking active status', err);
          this.isAccountActive.set(false);
        }
      });
  }

  activateAccount(): void {
    this.http.patch(environment.baseUrls['authservice.base-uri'] + `/auth-api/v1/user/activate/${this.dataStore.getUserId()}`, {})
      .subscribe({
        next: (response: any) => {
          console.log('user has successfully activated his account', response);
          this.router.navigate(['/activate-account']);
        },
        error: (error: any) => {
          this.errorMessage.set(error.errorMessage);
        }
      });
  }
}
