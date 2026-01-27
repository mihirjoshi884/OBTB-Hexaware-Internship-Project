import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth-service';
import { UserProfileService } from '../../core/services/UserProfileService.service';

@Component({
  selector: 'app-add-funds',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-funds.html'
})
export class AddFunds {
  @Output() complete = new EventEmitter<number>();
  @Output() closed = new EventEmitter<void>();

  private authService = inject(AuthService);
  private profileService = inject(UserProfileService);

  amount: number | null = null;
  quickAmounts = [500, 1000, 2000, 5000];
  isProcessing = false;
  errorMessage: string | null = null;

  setAmount(val: number) {
    this.amount = val;
    this.errorMessage = null;
  }

  processPayment() {
    if (!this.amount || this.amount <= 0) {
      this.errorMessage = 'Please enter a valid amount';
      return;
    }

    const username = this.authService.getUsername();
    if (!username) {
      this.errorMessage = 'Unable to identify user. Please login again.';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = null;

    // Call the actual API endpoint
    this.profileService.addFunds(username, this.amount).subscribe({
      next: (fundsSummary) => {
        console.log('💰 Funds added successfully:', fundsSummary);
        this.isProcessing = false;
        // Emit the amount that was successfully added
        this.complete.emit(this.amount!);
        // Reset form
        this.amount = null;
      },
      error: (err) => {
        console.error('❌ Error adding funds:', err);
        this.isProcessing = false;
        this.errorMessage = err.error?.message || 'Failed to add funds. Please try again.';
      }
    });
  }
}
