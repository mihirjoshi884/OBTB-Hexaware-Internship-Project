import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth-service';
import { UserProfileService } from '../../core/services/UserProfileService.service';

@Component({
  selector: 'app-withdraw-funds',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './withdraw-funds.html',
})
export class WithdrawFunds {

  @Input() currentBalance: number = 0;
  @Output() complete = new EventEmitter<number>();
  @Output() closed = new EventEmitter<null>();

  private readonly userService = inject(UserProfileService);
  private readonly authService = inject(AuthService);

  amount: number | null = null;
  isProcessing: boolean = false;
  errorMessage: string | null = null;
  
  processWithdrawal(){
    if(!this.amount || this.amount <= 0){
      this.errorMessage = "please enter valid amount";
      return;
    }
    if( this.amount > this.currentBalance){
      this.errorMessage = "Insufficient funds in the wallet";
      return;
    }
    const username = this.authService.getUsername();
    if(!username){
      this.errorMessage = "user not found or user is not logged in";
      return;
    }
    this.isProcessing = true;
    this.errorMessage = null;
    
    this.userService.withDrawFunds(username,this.amount).subscribe({
      next: (summary)=>{
        this.isProcessing = false;
        this.complete.emit(this.amount!);
        this.amount = null;
      },
      error: (err) => {
        this.isProcessing = false;
        this.errorMessage = err.error?.message || 'Withdrawal failed. Please try again.';
      }
    });
  }
}
