import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Tabs } from 'src/app/interfaces/tabs';
import { TransactionSkeletons } from 'src/app/shared/skeletons/transaction-skeletons/transaction-skeletons';
import { AuthService } from '../../core/services/auth-service';
import { UserProfileService } from '../../core/services/UserProfileService.service';
import { UserProfile } from '../../interfaces/user-profile';
import { AddFunds } from '../add-funds/add-funds';
import { ChangePasswordComponent } from '../change-password/change-password';
import { WithdrawFunds } from '../withdraw-funds/withdraw-funds';
import { EditUserProfile } from './edit-user-profile/edit-user-profile';
import { TransactionComponent } from './transaction-component/transaction-component';
import { UploadDocuments } from './upload-documents/upload-documents';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    AddFunds,
    WithdrawFunds, 
    ChangePasswordComponent, 
    EditUserProfile,
    TransactionComponent, 
    TransactionSkeletons,
    UploadDocuments
  ],
  templateUrl: 'user-dashboard.html',
  styles: []
})
export class UserDashboard implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly profileService = inject(UserProfileService);

  transactions: any[] = [];
  isHistoryLoading: boolean = false;
  
  
  activeTab: string = 'overview';

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

  loadHistory() {
    if (!this.user?.userId) return;
    this.isHistoryLoading = true;
    this.profileService.getTransactionHistory(this.user.userId, 0, 10).subscribe({
      next: (res: any) => {
        this.transactions = res.content;
        
        // 2. Artificial delay (optional) so the user actually sees the smooth transition
        setTimeout(() => {
          this.isHistoryLoading = false; // 3. Swap Skeletons for real data
          this.cdr.detectChanges();
        }, 800);
      },
      error: (err) => {
        this.isHistoryLoading = false;
        console.error(err);
      }
    });
  }
  Tabs: Tabs[] = []; 
  loadProfile(username: string) {
    this.isLoading = true;
    this.profileService.getUserProfile(username).subscribe({
      next: (userData: UserProfile) => {
        if (userData) {
          this.user = { ...userData };
          this.generateTabs(this.user.roleName);
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
  generateTabs(role: string){
    const commonTabs = [
      {id:'overview',label:'overview'},
      { id: 'security', label: 'Security' },
      { id: 'activity', label: 'Activity' },
    ];

    if(role === 'CUSTOMER'){
      this.Tabs = [...commonTabs, { id: 'my-wallet', label: 'My Wallet' }, { id: 'activity', label: 'My Bookings' }];
    }else if(role === 'BUS_OPERATOR'){
      this.Tabs = [
        ...commonTabs,
        { id: 'my-wallet', label: 'Earnings' },
        { id: 'upload-docs', label: 'Upload Documents' }, // For AI Docs
        { id: 'verification', label: 'verification' },      // For AI Face
        { id: 'manage-fleet', label: 'Bus Fleet' },
        { id: 'add-routes', label: 'Routes' }
      ];
    }else if(role === 'ADMIN'){
      this.Tabs = [...commonTabs, { id: 'admin-verify', label: 'Pending Approvals' }, { id: 'user-management', label: 'Users' }];
    }
  }


  switchTab(tabId: string): void {
    this.activeTab = tabId;
    
    // Logic for data-heavy tabs
    if (tabId === 'activity' || tabId === 'my-wallet') {
      this.loadHistory();
    }
  }

  toggleAddFunds() {
    this.isAddingFunds = !this.isAddingFunds;
  }

  isWithdrawingFunds: boolean = false;
    toggleWithdrawFunds() {
      this.isWithdrawingFunds = !this.isWithdrawingFunds;
  }

  onFundsWithdrawn(amount: number) {
      this.walletBalance -= amount;
      this.isWithdrawingFunds = false;
      this.loadHistory();
  }
  onFundsAdded(amount: number){
    this.walletBalance += amount;
    this.isAddingFunds = false;
    this.loadHistory();
  }
}