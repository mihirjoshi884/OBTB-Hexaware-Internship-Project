import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusFleetResponse, RouteResponse } from 'src/app/interfaces/bus-operator.models';
import { BusService } from '../../../../core/services/bus-service';
import { TripService } from '../../../../core/services/trip-service';
import { TripCreationRequest, TripType } from '../../../../interfaces/trip-model';

@Component({
  selector: 'app-trip-scheduler-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [BusService, TripService],
  templateUrl: './trip-scheduler-form.html'
})
export class TripSchedulerForm implements OnInit {

  @Input() companyId: string = '';
  @Output() onSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  buses: BusFleetResponse[] | null = null;
  routes: RouteResponse[] | null = null;

  is_busLoading = false;
  is_busRoutesLoading = false;
  is_submitting = false;

  // Selection Variables (UI State)
  selectedBusId: string = "";
  selectedRouteId: string = "";
  public TripType = TripType;
  tripTypes = Object.values(TripType);
  selectedTripType: TripType = TripType.ONE_TIME;
  selectedDay: string = "MONDAY";
  daysOfWeek: string[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
  
  // Picker Variables (Strings for HTML5 input compatibility)
  selectedDepartureTime: string = "";
  selectedArrivalTime: string = "";
  selectedDepartureDate: string = "";
  selectedArrivalDate: string = "";
  selectedRegularDepartureTime: string = "";
  baseFare: number = 0;

  constructor(
    @Inject(TripService) private readonly tripService: TripService,
    private readonly busService: BusService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.companyId) {
      this.fetchBuses(this.companyId);
      this.fetchRoutes(this.companyId);
    }
  }

  // --- UI Event Handlers ---

  onBusSelect(event: Event): void {
    this.selectedBusId = this.getElementValue(event);
  }

  onRouteSelect(event: Event): void {
    this.selectedRouteId = this.getElementValue(event);
  }

  onDaySelect(event: Event): void {
    this.selectedDay = this.getElementValue(event);
  }

  onTripTypeSelect(event: Event): void {
    this.selectedTripType = this.getElementValue(event) as TripType;
  }

  onDepartureDateInput(event: Event): void {
    this.selectedDepartureDate = this.getElementValue(event);
  }

  onDepartureTimeInput(event: Event): void {
    this.selectedDepartureTime = this.getElementValue(event);
  }

  onArrivalDateInput(event: Event): void {
    this.selectedArrivalDate = this.getElementValue(event);
  }

  onArrivalTimeInput(event: Event): void {
    this.selectedArrivalTime = this.getElementValue(event);
  }

  onRegularDepartureTimeInput(event: Event): void {
    this.selectedRegularDepartureTime = this.getElementValue(event);
  }

  // --- Data Fetching ---

  fetchBuses(companyId: string) {
    this.is_busLoading = true;
    this.busService.fetchBuses(companyId).subscribe({
      next: response => {
        this.buses = [...response.body];
        this.is_busLoading = false;
        this.cdr.detectChanges();
      },
      error: error => {
        this.is_busLoading = false;
        console.error("Bus fetch failed:", error);
        this.cdr.detectChanges();
      }
    });
  }

  fetchRoutes(companyId: string) {
    this.is_busRoutesLoading = true;
    this.busService.fetchAllExistingCompanyRoute(companyId).subscribe({
      next: response => {
        this.routes = [...response.body];
        this.is_busRoutesLoading = false;
        this.cdr.detectChanges();
      },
      error: error => {
        this.is_busRoutesLoading = false;
        console.error("Route fetch failed:", error);
        this.cdr.detectChanges();
      }
    });
  }

  // --- Submission ---

  isTripFormValid(): boolean {
    const hasCoreSelections = this.hasValue(this.selectedBusId)
      && this.hasValue(this.selectedRouteId)
      && this.hasValidFare();

    if (!hasCoreSelections) {
      return false;
    }

    if (this.selectedTripType === TripType.REGULAR) {
      return this.hasValue(this.selectedDay) && this.hasValue(this.selectedRegularDepartureTime);
    }

    return this.hasValue(this.selectedDepartureDate)
      && this.hasValue(this.selectedDepartureTime)
      && this.hasValue(this.selectedArrivalDate)
      && this.hasValue(this.selectedArrivalTime);
  }

  submit(): void {
    if (!this.isTripFormValid()) {
      alert('Please fill all required trip details before submitting.');
      return;
    }

    this.is_submitting = true;
    const isOneTimeTrip = this.selectedTripType === TripType.ONE_TIME;
    const request: TripCreationRequest = {
      routeId: this.selectedRouteId,
      busId: this.selectedBusId,
      companyId: this.companyId,
      tripType: this.selectedTripType,
      departureDate: isOneTimeTrip ? this.toNullableValue(this.selectedDepartureDate) : null,
      departureTime: isOneTimeTrip ? this.toNullableValue(this.selectedDepartureTime) : null,
      arrivalDate: isOneTimeTrip ? this.toNullableValue(this.selectedArrivalDate) : null,
      arrivalTime: isOneTimeTrip ? this.toNullableValue(this.selectedArrivalTime) : null,
      scheduledDay: isOneTimeTrip ? null : this.toNullableValue(this.selectedDay),
      regularDepartureTime: isOneTimeTrip ? null : this.toNullableValue(this.selectedRegularDepartureTime),
      baseFare: Number(this.baseFare)
    };

    console.log(request);

    this.tripService.createTrip(request).subscribe({
      next: () => {
        console.log("Trip created successfully:", request);
        this.onSuccess.emit();
        this.is_submitting = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Creation failed', err);
        this.is_submitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  private getElementValue(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement | null)?.value ?? '';
  }

  private hasValue(value: string | null | undefined): boolean {
    return this.toNullableValue(value) !== null;
  }

  private hasValidFare(): boolean {
    return this.baseFare !== null
      && this.baseFare !== undefined
      && !Number.isNaN(Number(this.baseFare))
      && Number(this.baseFare) >= 0;
  }

  private toNullableValue(value: string | null | undefined): string | null {
    const normalizedValue = value?.trim() ?? '';
    return normalizedValue.length > 0 ? normalizedValue : null;
  }
}
