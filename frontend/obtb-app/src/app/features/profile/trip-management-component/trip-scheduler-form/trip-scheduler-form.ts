import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TripService } from '../../../../core/services/trip-service'; 
import { tripCreationRequest, TripType } from '../../../../interfaces/trip-model';

@Component({
  selector: 'app-trip-scheduler-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trip-scheduler-form.html'
})
export class TripSchedulerForm {
  @Input() companyId: string = '';
  @Output() onSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private tripService: TripService = inject(TripService);

  // Initialize with Date objects to satisfy the interface
  request: tripCreationRequest = {
    routeId: '',
    busId: '',
    companyId: '',
    tripType: TripType.REGULAR, 
    firstDepartureTime: new Date(),
    firstArrivalTime: new Date(),
    scheduledDay: 'MONDAY',
    dailyDepartureTime: '',
    baseFare: 0
  };
  onDateChange(event: string, field: 'firstDepartureTime' | 'firstArrivalTime') {
    if (event) {
      this.request[field] = new Date(event);
    }
  }
  submit() {
    this.request.companyId = this.companyId;
    
    // Note: If your API expects ISO strings, you might need 
    // to transform these Dates before sending, but this satisfies 
    // the TS compiler for now.
    this.tripService.createTrip(this.request).subscribe({
      next: () => this.onSuccess.emit(),
      error: (err: any) => console.error('Creation failed', err)
    });
  }
}