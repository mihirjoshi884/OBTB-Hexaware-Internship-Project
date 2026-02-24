import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { BusService } from 'src/app/core/services/bus-service';
import { BusCreationResponse, BusFleetResponse } from 'src/app/interfaces/bus-operator.models';

@Component({
  selector: 'app-fleet-list-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fleet-list-component.html'
})
export class FleetListComponent implements OnInit, OnChanges {

  constructor(
    private readonly busService: BusService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  @Input() companyId!: string | undefined;
  @Input() userId!: string;
  @Input() newlyCreatedBus?: BusCreationResponse | null;

  // Now using the nested interface we defined
  buses: BusFleetResponse[] = [];
  loading = false;

  ngOnInit(): void {
    this.fetchBusFleet();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['companyId'] && this.companyId && !this.buses.length && !this.loading) {
      this.fetchBusFleet();
    }

    /* TODO: Handle newly created bus structure mapping
      The structure of BusCreationResponse doesn't match BusFleetResponse yet.
      Commenting this out to prevent UI crashes.
    */
    /*
    if (changes['newlyCreatedBus'] && this.newlyCreatedBus) {
       this.fetchBusFleet(); // Refresh the whole list instead for now
    }
    */
  }

  fetchBusFleet() {
    if (!this.companyId) return;

    this.loading = true;
    // We expect the backend to return the nested structure now
    this.busService.fetchBuses(this.companyId).subscribe({
      next: (response: any) => {
        this.buses = response.body || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        console.error(error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // FIXED: Added this method to resolve the TS2339 Template Error
  onEditBus(busId: string) {
    console.log("Edit requested for bus:", busId);
    // You can use this busId to navigate: this.router.navigate(['/edit', busId]);
  }

  getStatusBadge(bus: BusFleetResponse): { text: string; color: string } {
    // Basic placeholder for status logic
    return { text: 'NO DOCUMENTS', color: 'bg-yellow-600' };
  }
}