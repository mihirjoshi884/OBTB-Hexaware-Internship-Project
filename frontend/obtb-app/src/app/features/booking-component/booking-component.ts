import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth-service';
import { BookingService } from 'src/app/core/services/booking-service';
import { EnrichedSeatDto, PassengerDetailDto, PrimaryPassangerDetailCreationRequest, PrimaryPassangerDetailDto, SeatStatus, SeatType } from 'src/app/interfaces/booking-interfaces';
import { TripSearchResponseDto } from 'src/app/interfaces/search-interface';
import { PrimaryPassangerFormComponent } from '../booking-component/primary-passanger-form-component/primary-passanger-form-component';
import { PassengerFormComponent } from './passenger-form-component/passenger-form-component';

@Component({
  selector: 'app-booking-component',
  standalone: true,
  imports: [CommonModule, PrimaryPassangerFormComponent, PassengerFormComponent],
  templateUrl: './booking-component.html'
})
export class BookingComponent implements OnInit {

  public SeatStatus = SeatStatus;
  public SeatType = SeatType;

  currentStep: 'SEATS' | 'PROFILE' | 'PASSENGERS' | 'POLLING' | 'SUCCESS' | 'FAILED' = 'SEATS';

  bus: TripSearchResponseDto | null = null;
  seats: EnrichedSeatDto[] = [];
  selectedSeats: EnrichedSeatDto[] = [];
  viewMore: boolean = false;
  isProcessing: boolean = false;
  userId: string = this.authService.getUserId(); // Inject from your logged-in Auth State
  primaryProfile!: PrimaryPassangerDetailCreationRequest;
  primaryProfileResponse!: PrimaryPassangerDetailDto;

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

    this.primaryProfile = {
      userId: this.authService.getUserId(),
      name: '',
      phone: '',
      email: '',
      emergencyContactName: '',
      emergencyContact: ''
    };
    
    this.primaryProfileResponse = {
      userId: '',
      name: '',
      email: ''
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
    console.log("Seats selected are:", this.selectedSeats);
    console.log("Total fare is:", this.totalFare);

    // Call API to inspect if profile needs to be created
    this.bookingService.checkProfileAvailability(this.userId).subscribe({
      next: (exists: boolean) => {
        if (exists) {
          this.currentStep = 'PASSENGERS';
          this.cdr.detectChanges();
          // Initialize passenger forms logic goes here
        } else {
          this.currentStep = 'PROFILE';
          this.cdr.detectChanges();
        }
      },
      error: error => {
        if (error.status === 404){
          console.log("No primary passenger found (404). Directing to profile creation.");
          this.currentStep = 'PROFILE';
          this.cdr.detectChanges();
        }else {
          // Keep the alert active for real failures (500 errors, network loss, etc.)
          console.error('Error checking profile status:', error);
          alert('Failed to check profile status.');
        }
      }
    });
  }

  onCreateProfile(completedProfile: PrimaryPassangerDetailCreationRequest) {
    this.isProcessing = true;
    this.bookingService.createProfile(completedProfile).subscribe({
      next: (response) => {
        if (response && response.body) {
          this.primaryProfileResponse = response.body; 
        } else {
          this.primaryProfile = completedProfile; 
        }
        this.isProcessing = false;
        this.currentStep = 'PASSENGERS';
        this.cdr.detectChanges();
      },
      error: () => {
        alert('Failed to create profile.');
        this.isProcessing = false;
        this.cdr.detectChanges();
      }
    });
  }

  onPassengersSubmitted(eventData: { passengers: PassengerDetailDto[], files: File[] }) {
    this.isProcessing = true;
    this.cdr.detectChanges();

    const formData = new FormData();

    const bookingRequest = {
      tripInstanceId: this.bus?.instanceId, 
      source: this.bus?.source,            
      destination: this.bus?.destination,
      userId: this.userId,
      passengers: eventData.passengers.map((p: any) => ({
        passengerName: p.name, 
        age: p.age,
        gender: p.gender,
        seatNumber: p.seatNumber,
        idProofType: p.idType,
        idNumber: p.idNumber
      }))
    };

    formData.append('request', JSON.stringify(bookingRequest));

    eventData.files.forEach(file => {
      formData.append('idFiles', file);
    });

    // Fire the API!
    this.bookingService.bookTicket(formData).subscribe({
      next: (res) => {
        console.log("Booking initiated successfully!", res);
        this.isProcessing = false;
        this.cdr.detectChanges();

        if (res && res.body && res.body.redirectUrl) {
          // Extract the local path & query params from the full URL string generated by backend
          const url = new URL(res.body.redirectUrl);
          const bookingId = url.searchParams.get('bookingId');
          
          // Use Angular router for a smooth SPA transition without reloading the browser
          this.router.navigate(['/payment'], {
            queryParams: { 
              bookingId: bookingId,
              pnr: res.body.pnr,
              source: res.body.source,
              destination: res.body.destination,
              amountToPay: res.body.amountToPay
            }
          });
          
        }
      },
      error: (err) => {
        console.error("Booking failed:", err);
        alert('Failed to initiate ticket booking.');
        this.isProcessing = false;
        this.cdr.detectChanges();
      }
    });
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
    return (this.bus?.fare || 0) * (this.selectedSeats.length || 0);
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
        const rawSeats = res?.body?.seats ?? res?.seats ?? [];

        this.seats = rawSeats.map((s: any) => ({
          ...s,
          seatType:   s.seatType   ?? s.type,
          seatStatus: s.seatStatus ?? s.status,
        }));

        console.log('Mapped seats:', this.seats.length, this.seats[0]);
        this.cdr.detectChanges(); 
      },
      error: (err) => console.error('Seat mapping error', err)
    });
  }
}