import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';
import { BookingService } from 'src/app/core/services/booking-service';
import { EnrichedSeatDto, SeatStatus, SeatType } from 'src/app/interfaces/booking-interfaces';
import { TripSearchResponseDto } from 'src/app/interfaces/search-interface';

@Component({
  selector: 'app-booking-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-component.html'
})
export class BookingComponent implements OnInit {

  public SeatStatus = SeatStatus;
  public SeatType = SeatType;

  bus: TripSearchResponseDto | null = null;
  seats: EnrichedSeatDto[] = [];
  selectedSeats: EnrichedSeatDto[] = [];
  viewMore: boolean = false;
  constructor(
    private readonly router: Router,
    private readonly bookingService: BookingService,
    private readonly authService: AuthService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const cachedData = sessionStorage.getItem('selected_bus_cache');
    if (cachedData) {
      this.bus = JSON.parse(cachedData);
    } else {
      this.router.navigate(['/home']);
      return;
    }
    if (this.authService.isLoggedIn()) {
      this.viewMore = true;
      this.fetchSeatMapping();
    }
  }

  onViewMore() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
    } else {
      this.viewMore = true;
      this.fetchSeatMapping();
    }
  }

  onProceedToCheckout() {
    console.log("seats selected are",this.selectedSeats);
    console.log("total fair is",this.totalFare);
    // sessionStorage.setItem('selected_seats_cache', JSON.stringify(this.selectedSeats));
    // this.router.navigate(['/checkout']);
  }

  // --- Template Helpers ---

  getColumns(deck?: number): number {
    const seats = deck !== undefined
      ? this.seats.filter(s => +s.deck === deck)
      : this.seats;
    if (!seats.length) return 5;
    return Math.max(...seats.map(s => +s.x_coordinate)) + 1;
  }

  getSeatsByDeck(deck: number): EnrichedSeatDto[] {
    const deckSeats = this.seats.filter(s => +s.deck === deck);
    if (!deckSeats.length) return [];
    const minY = Math.min(...deckSeats.map(s => +s.y_coordinate));
    return deckSeats.map(s => ({ ...s, y_coordinate: +s.y_coordinate - minY }));
  }

  hasSeatsInDeck(deck: number): boolean {
    return this.seats.some(s => +s.deck === deck && s.seatType !== SeatType.WALKWAY);
  }

  getAvailableCount(deck: number): number {
    return this.seats.filter(
      s => +s.deck === deck &&
           s.seatType !== SeatType.WALKWAY &&
           s.seatStatus === SeatStatus.AVAILABLE
    ).length;
  }

  getNonWalkwayCount(deck: number): number {
    return this.seats.filter(
      s => +s.deck === deck && s.seatType !== SeatType.WALKWAY
    ).length;
  }

  get totalFare(): number {
    return (this.bus?.fare || 0) * (this.selectedSeats.length || 1);
  }

  toggleSeat(seat: EnrichedSeatDto) {
    if (seat.seatType === SeatType.WALKWAY || seat.seatStatus !== SeatStatus.AVAILABLE) return;
    const index = this.selectedSeats.findIndex(s => s.tripSeatId === seat.tripSeatId);
    if (index > -1) {
      this.selectedSeats.splice(index, 1);
    } else {
      this.selectedSeats.push(seat);
    }
  }

  isSelected(seat: EnrichedSeatDto): boolean {
    return this.selectedSeats.some(s => s.tripSeatId === seat.tripSeatId);
  }

  private fetchSeatMapping() {
    if (!this.bus?.instanceId) return;

    this.bookingService.getSeatMapping(this.bus.instanceId).subscribe({
      next: (res: any) => {
        // Handle both res.body.seats and res.seats (in case wrapper is transparent)
        const rawSeats = res?.body?.seats ?? res?.seats ?? [];

        this.seats = rawSeats.map((s: any) => ({
          ...s,
          seatType:   s.seatType   ?? s.type,
          seatStatus: s.seatStatus ?? s.status,
        }));

        console.log('Mapped seats:', this.seats.length, this.seats[0]);
        this.cdr.detectChanges(); // force Angular to re-render
      },
      error: (err) => console.error('Seat mapping error', err)
    });
  }
}