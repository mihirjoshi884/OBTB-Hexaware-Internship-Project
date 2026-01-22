import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { delay, of } from 'rxjs'; // Used to simulate backend response
import { UserProfile } from '../../../interfaces/user-profile';
import { ChangePasswordComponent } from '../../change-password/change-password';
import { EditUserProfile } from '../edit-user-profile/edit-user-profile';

@Component({
    selector: 'app-mock-profile',
    standalone: true,
    imports: [CommonModule, ChangePasswordComponent, EditUserProfile],
    templateUrl: './mock-profile.html',
    })
    export class mockProfile implements OnInit {
    private readonly cdr = inject(ChangeDetectorRef);
    private readonly router = inject(Router);
    
    activeTab: 'overview'| 'security' | 'activity' | 'my-wallet' ='overview';
    user: UserProfile | null = null;
    walletBalance: number = 0;
    isAddingFunds: boolean = false;
    isLoading: boolean = true;
    is_Editing: boolean = false;

    // 1. HARDCODED DUMMY DATA
    private mockUser: UserProfile = {
        username: 'MikeJuliet884',
        firstName: 'Mihir',
        lastName: 'Kumar',
        roleName: 'CUSTOMER',
        gender: ' ',
        dateOfBirth: '1998-05-15',
        walletBalance: 15400.75,
        contact: '9876543210',
        email: 'mihir.dev@example.com',
        profilePictureUrl: '', // Empty string tests the "Initial Avatar" logic
        createdAt: '2023-01-01T10:00:00Z',
        passwordLastUpdated: '2023-12-01T10:00:00Z',
        lastLogin: '2026-01-19T11:00:00Z'
    };

    ngOnInit(): void {
        console.log('🛠️ Mock Mode Active: Bypassing AuthService and Backend');
        this.loadProfile();
    }

    onProfileUpdate(event: { data: any, file: File | null }) {
        const { data, file } = event; // Destructure the package from the child
        
        // 1. Create the "Shipping Crate"
        const formData = new FormData();
        
        // 2. Add the Image (if the user picked one)
        if (file) {
            formData.append('profileImage', file);
        }
        
        // 3. Add the User Data (Gender, DOB, Contact)
        // We convert it to a string because FormData only likes Strings or Blobs
        formData.append('updateFields', JSON.stringify(data));

        console.log('📡 Dispatching FormData to Backend...');

        // 4. Call your Service (Simulated for now)
        // this.userService.updateProfile(formData).subscribe({
        //     next: (response) => {
        //         // Only update the UI once the backend says "OK"
        //         if (this.user) {
        //             this.user = { ...this.user, ...data };
        //             // If backend returns a new URL for the image, update it too
        //             if (response.profilePictureUrl) {
        //                 this.user.profilePictureUrl = response.profilePictureUrl;
        //             }
        //         }
        //         this.is_Editing = false;
        //         this.cdr.detectChanges();
        //         console.log('✅ Backend Update Successful');
        //     },
        //     error: (err) => {
        //         console.error('❌ Backend Update Failed:', err);
        //         // Alert the user that the save failed
        //     }
        // });
    }

    loadProfile() {
        this.isLoading = true;
        
        // 2. SIMULATE API CALL
        // We use 'of' to create an observable and 'delay' to simulate network latency
        of(this.mockUser).pipe(delay(1500)).subscribe({
        next: (userData) => {
            this.user = { ...userData };
            this.walletBalance = userData.walletBalance;
            
            this.isLoading = false;
            // Crucial: Manually trigger detection because of the async delay
            this.cdr.detectChanges(); 
            
            console.log('✅ Mock Data loaded successfully:', this.user);
        },
        error: (err) => {
            this.isLoading = false;
            this.cdr.detectChanges();
            console.error('❌ Mock Loading Error:', err);
        }
        });
    }

    switchTab(tab: 'overview'| 'security' | 'activity' | 'my-wallet'): void {
        this.activeTab = tab;
        console.log(`Switched to ${tab} tab`);
    }

    toggleAddFunds() {
        this.isAddingFunds = !this.isAddingFunds;
    }

    onFundsAdded(amount: number) {
        this.walletBalance += amount;
        this.isAddingFunds = false;
        this.cdr.detectChanges();
    }
}