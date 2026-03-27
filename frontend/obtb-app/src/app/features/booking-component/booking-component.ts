import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';
import { SearchService } from 'src/app/core/services/search-service';
import { TripSearchResponseDto } from 'src/app/interfaces/search-interface';

@Component({
  selector: 'app-booking-component',
  imports: [CommonModule],
  templateUrl: './booking-component.html'
})
export class BookingComponent {


  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly searchService: SearchService,
    private readonly authService: AuthService
  ){}

  ngOnInit() {
    // 1. Retrieve the cached bus from session
    const cachedData = sessionStorage.getItem('selected_bus_cache');
    if (cachedData) {
      this.bus = JSON.parse(cachedData);
    } else {
      this.router.navigate(['/home']); // Safety: go back if no bus is selected
    }

    // 2. Check if the user is already logged in (e.g., just returned from Login)
    if (this.authService.isLoggedIn()) {
      this.viewMore = true;
    }
  }

  bus: TripSearchResponseDto | null = null;
  viewMore: boolean = false;

  onViewMore() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
    } else {
      this.viewMore = true;
    }
  }
}
