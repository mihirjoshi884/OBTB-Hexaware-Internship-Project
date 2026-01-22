import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/Auth-Services/auth-service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isLoggedIn() {
    return this.authService.isLoggedIn();
  }

  getUsername() {
    return this.authService.getUsername();
  }

  // Form state
  tripType = 'oneway';
  fromLocation = '';
  toLocation = '';
  departDate = '';
  returnDate = '';

  searchBuses(): void {
    if (this.fromLocation && this.toLocation && this.departDate) {
      console.log('Searching for buses:', {
        from: this.fromLocation,
        to: this.toLocation,
        departDate: this.departDate,
        returnDate: this.returnDate,
        tripType: this.tripType
      });
      // Integration with backend API will be implemented in future updates
      alert('Search functionality coming soon! Your search parameters have been logged.');
    } else {
      alert('Please fill in all required fields');
    }
  }

  resetSearch(): void {
    this.tripType = 'oneway';
    this.fromLocation = '';
    this.toLocation = '';
    this.departDate = '';
    this.returnDate = '';
  }
}
