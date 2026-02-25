import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, Input, NO_ERRORS_SCHEMA, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { forkJoin } from 'rxjs';
import { BusService } from 'src/app/core/services/bus-service';
import { AddBusStaffRequest, BusCreationResponse, BusFleetResponse, BusStaffResponse } from 'src/app/interfaces/bus-operator.models';
import { ManageStaffModalComponent } from '../../bus-staff-component/manage-staff-modal';
import { BusLayoutPreviewComponent } from '../bus-layout-preview.component/bus-layout-preview.component';

@Component({
  selector: 'app-fleet-list-component',
  standalone: true,
  imports: [CommonModule, ManageStaffModalComponent, BusLayoutPreviewComponent],
  schemas: [NO_ERRORS_SCHEMA],
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

  // Staff management drawer
  selectedBusForEdit: BusFleetResponse | null = null;
  availableCompanyStaff: BusStaffResponse[] = [];
  currentBusStaff: AddBusStaffRequest[] = [];
  staffDrawerOpen = false;

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
    const foundBus = this.buses.find(b => b.busId === busId);
    if (foundBus) {
        // Deep clone or re-assign to trigger a fresh reference
        this.selectedBusForEdit = { ...foundBus };
        console.log(this.selectedBusForEdit?.template?.layoutData) 
        this.staffDrawerOpen = true;
        this.fetchCompanyStaff();
        
        // Let the cycle finish then detect
        setTimeout(() => {
            this.cdr.detectChanges();
        }, 0);
    }
  }

  fetchCompanyStaff(): void {
    if (!this.companyId) return;

    this.busService.fetchCompanyStaff(this.companyId).subscribe({
      next: response => {
        // Create a new array reference to trigger Change Detection in the child modal
        this.availableCompanyStaff = [...(response.body || [])];
        console.log('Staff fetched:', this.availableCompanyStaff.length);
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      },
      error: error => console.error('Staff fetch error:', error)
    });
  }

  closeStaffDrawer(): void {
    this.staffDrawerOpen = false;
    this.selectedBusForEdit = null;
    this.cdr.detectChanges();
  }

  onStaffSaved(assignments: AddBusStaffRequest[]): void {
    if (!assignments || assignments.length === 0) {
        this.closeStaffDrawer();
        return;
    }

    this.loading = true; // Show loading state

    // Create an array of Observables for each staff update
    const updateRequests = assignments.map(staff => 
        this.busService.updateBusStaff(staff)
    );

    // Use forkJoin to wait for all updates to finish
    forkJoin(updateRequests).subscribe({
        next: (results) => {
            console.log('All staff updated successfully', results);
            this.loading = false;
            
            // Refresh the fleet list to show updated assignments
            this.fetchBusFleet(); 
            this.closeStaffDrawer();
            
            // Optional: Show a success toast/notification here
        },
        error: (err) => {
            console.error('Error updating staff:', err);
            this.loading = false;
            // You might want to pass this error back to the modal's saveError input
        }
    });
  }

  getStatusBadge(bus: BusFleetResponse): { text: string; color: string } {
    // Basic placeholder for status logic
    return { text: 'NO DOCUMENTS', color: 'bg-yellow-600' };
  }

  getBusLayoutData(): any {
    if (this.selectedBusForEdit && this.selectedBusForEdit.template) {
      // Return the layoutData string from the template
      return this.selectedBusForEdit.template.layoutData;
    }
    return null;
  }
}