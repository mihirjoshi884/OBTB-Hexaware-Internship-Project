import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, interval, takeUntil } from 'rxjs';
import { BookingService } from 'src/app/core/services/booking-service';
import { BookingInitiatedResponse } from 'src/app/interfaces/booking-interfaces';

@Component({
  selector: 'app-payment-component',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './payment-component.html'
})
export class PaymentComponent implements OnInit, OnDestroy {

  bookingId: string | null = null;
  bookingData?: BookingInitiatedResponse;
  
  paymentInitiated: boolean = false;
  currentStatus: 'WAITING' | 'CONFIRMED' | 'FAILED' | 'UNKNOWN' = 'WAITING';
  currentStep: 'summary' | 'processing' = 'summary';

  // ADDED: Variables to satisfy Angular compiler and track polling
  pollCount: number = 0;
  readonly MAX_POLL_ATTEMPTS: number = 20; // Stops after 30 seconds (20 * 1.5s)
  
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly bookingService: BookingService,
    private readonly cdr: ChangeDetectorRef
  ) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state) {
      this.bookingData = navigation.extras.state['bookingResponse'];
    }
  }

  ngOnInit() {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.bookingId = params['bookingId'];
      
      if (this.bookingId) {
        this.bookingData = {
          pnr: params['pnr'],
          source: params['source'],
          destination: params['destination'],
          amountToPay: params['amountToPay']
        } as any;
      }
    });
  }

  onPayNow() {
    if (this.bookingId) {
      this.currentStep = 'processing';
      this.cdr.detectChanges();
      this.startPaymentFlow(this.bookingId);
    }
  }

  private startPaymentFlow(id: string) {
    this.bookingService.initiatePayment(id).subscribe({
      next: (res) => {
        if (res && res.body === true) {
          this.paymentInitiated = true;
          this.cdr.detectChanges();
          this.pollBookingStatus(id);
        }
      },
      error: (err) => {
        console.error('Failed to initiate payment:', err);
        this.currentStatus = 'FAILED';
        this.cdr.detectChanges();
      }
    });
  }

  private pollBookingStatus(id: string) {
    interval(1500)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        // INCREMENT: Increase the attempt count on each tick
        this.pollCount++;

        // CHECK: If we hit the max ceiling, fail gracefully
        if (this.pollCount >= this.MAX_POLL_ATTEMPTS) {
          console.warn('Polling timed out.');
          this.currentStatus = 'FAILED';
          this.cdr.detectChanges();
          this.destroy$.next(); // Stop the interval
          return;
        }

        this.bookingService.getBookingStatus(id).subscribe({
          next: (res) => {
            const statusFromBackend = res.body;
            
            if (statusFromBackend) {
              this.currentStatus = statusFromBackend as any;
              this.cdr.detectChanges();

              if (this.currentStatus === 'CONFIRMED' || this.currentStatus === 'FAILED') {
                this.destroy$.next(); 
              }
            }
          },
          error: (err) => {
            console.error('Error while polling status:', err);
          }
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}