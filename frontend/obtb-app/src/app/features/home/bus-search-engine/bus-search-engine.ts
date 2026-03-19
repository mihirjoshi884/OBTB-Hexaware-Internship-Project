
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-bus-search-engine',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bus-search-engine.html'
})
export class BusSearchEngine {

  constructor(
    private readonly authService: AuthService
  ){}

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
