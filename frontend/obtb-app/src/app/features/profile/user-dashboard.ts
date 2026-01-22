import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/Auth-Services/auth-service';
import { UserProfileService } from '../../core/services/User-Services/UserProfileService.service';
import { UserProfile } from '../../interfaces/user-profile';
import { AddFunds } from '../add-funds/add-funds';
import { ChangePasswordComponent } from '../change-password/change-password';
import { EditUserProfile } from './edit-user-profile/edit-user-profile';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, AddFunds, ChangePasswordComponent, EditUserProfile],
  templateUrl: './user-dashboard.html',
  styles: []
})
export class UserDashboard implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly profileService = inject(UserProfileService);
  
  activeTab: 'overview'| 'security' | 'activity' | 'my-wallet' ='overview';

  user: UserProfile | null = null;
  walletBalance: number = 0;
  isAddingFunds: boolean = false;
  isLoading: boolean = true;
  is_Editing: boolean = false; // Added from Mock logic
  isSaving: boolean = false;   // Added to track backend sync status

  ngOnInit(): void {
    const username = this.authService.getUsername();
    if (username) {
      this.loadProfile(username);
    } else {
      console.warn('No username found in OIDC claims');
      this.router.navigate(['/login']);
    }
  }

  // UPDATED: Now handles the actual backend sync
  onProfileUpdate(event: { data: any, file: File | null }) {
    if (!this.user?.username) return;

    const { data, file } = event;
    this.isSaving = true;

    const formData = new FormData();
    
    // 1. Matches @RequestPart(value = "profileImage")
    if (file) {
      formData.append('profileImage', file);
    }

    const updateRequestPayload = {
      username: this.user.username,
      firstName: data.firstName,
      lastName: data.lastName,
      gender: data.gender,
      contact: data.contact,
      email: data.email,
      dateOfBirth: data.dateOfBirth
    }

    formData.append('updatedFields', JSON.stringify(updateRequestPayload)); 

    this.profileService.updateUserProfile(this.user.username, formData).subscribe({
      next: (updatedUser: UserProfile) => {
        this.user = { ...updatedUser };
        this.is_Editing = false;
        this.isSaving = false;
        this.cdr.detectChanges();
        console.log('✅ Profile updated successfully');
      },
      error: (err) => {
        this.isSaving = false;
        this.cdr.detectChanges();
        console.error('❌ Update failed:', err);
      }
    });
  }

  loadProfile(username: string) {
    this.isLoading = true;
    this.profileService.getUserProfile(username).subscribe({
      next: (userData: UserProfile) => {
        if (userData) {
          this.user = { ...userData };
          this.walletBalance = userData.walletBalance ?? 0;
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.cdr.detectChanges();
        console.error('❌ Data Mapping Error:', err);
      }
    });
  }

  switchTab(tab: 'overview'| 'security' | 'activity' | 'my-wallet'): void{
    this.activeTab = tab;
  }

  toggleAddFunds() {
    this.isAddingFunds = !this.isAddingFunds;
  }

  onFundsAdded(amount: number){
    this.walletBalance += amount;
    this.isAddingFunds = false;
  }
}