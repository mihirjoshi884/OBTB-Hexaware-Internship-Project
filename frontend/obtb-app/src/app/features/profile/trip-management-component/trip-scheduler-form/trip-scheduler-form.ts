import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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

  // Selection Variables (UI State)
  selectedBusId: string = "";
  selectedRouteId: string = "";
  public TripType = TripType;
  tripTypes = Object.values(TripType);
  selectedTripType: TripType = TripType.ONE_TIME;
  selectedDay: string = "MONDAY";
  daysOfWeek: string[] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
  
  // Picker Variables (Strings for HTML5 input compatibility)
  selectedDepartureTime: string | null = null;
  selectedArrivalTime: string | null = null;
  selectedDepartureDate: string | null = null;
  selectedArrivalDate: string | null = null;
  selectedRegularDepartureTime: string | null = null;
  baseFare: number = 0;

  constructor(
    private readonly tripService: TripService,
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

  onBusSelect(event: any): void {
    this.selectedBusId = event.target.value;
  }

  onRouteSelect(event: any): void {
    this.selectedRouteId = event.target.value;
  }

  onDaySelect(event: any): void {
    this.selectedDay = event.target.value;
  }

  onTripTypeSelect(event: any): void {
    this.selectedTripType = event.target.value;
  }

  // --- Data Fetching ---

  fetchBuses(companyId: string) {
    this.is_busLoading = true;
    this.busService.fetchBuses(companyId).subscribe({
      next: response => {
        this.buses = [...response.body];
        this.is_busLoading = false;
      },
      error: error => {
        this.is_busLoading = false;
        console.error("Bus fetch failed:", error);
      }
    });
  }

  fetchRoutes(companyId: string) {
    this.is_busRoutesLoading = true;
    this.busService.fetchAllExistingCompanyRoute(companyId).subscribe({
      next: response => {
        this.routes = [...response.body];
        this.is_busRoutesLoading = false;
      },
      error: error => {
        this.is_busRoutesLoading = false;
        console.error("Route fetch failed:", error);
      }
    });
  }

  // --- Submission ---

  submit() {
    // Constructing the DTO right before the API call ensures we have the latest UI state
    const request: TripCreationRequest = {
      routeId: this.selectedRouteId,
      busId: this.selectedBusId,
      companyId: this.companyId,
      tripType: this.selectedTripType,
      departureDate: this.selectedDepartureDate as any, 
      departureTime: this.selectedDepartureTime as any,
      arrivalDate: this.selectedArrivalDate as any,
      arrivalTime: this.selectedArrivalTime as any,
      scheduledDay: this.selectedDay,
      regularDepartureTime: this.selectedRegularDepartureTime as any,
      baseFare: this.baseFare
    };

    

    this.tripService.createTrip(request).subscribe({
      next: () => {
        console.log("Trip created successfully:", request);
        this.onSuccess.emit();
      },
      error: (err: any) => {
        console.error('Creation failed', err);
      }
    });
  }
}