import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import { RouteRequest, RouteResponse, RouteStopDto } from 'src/app/interfaces/bus-operator.models';

@Component({
  selector: 'app-route-management-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './route-management-component.html'
})
export class RouteManagementComponent implements OnInit {
  private readonly busService = inject(BusService);
  private readonly cdr = inject(ChangeDetectorRef);

  @Input() companyId: string = '';
  @Input() userId: string = '';

  routeList: RouteResponse[] = [];
  selectedRoute: RouteResponse | null = null;
  
  // UI States
  activeTab: 'inventory' | 'editor' = 'inventory';
  is_Editing: boolean = false;
  is_Creating: boolean = false;
  isLoadingRoutes: boolean = false;
  isSavingRoute: boolean = false;
  isDeletingRoute: boolean = false;
  hoveredRouteId: string | null = null;

  routeForm: RouteRequest = {
    companyId: '',
    routeName: '',
    origin: '',
    destination: '',
    stops: [],
    totalDistance: 0,
    estimatedDuration: 0
  };

  ngOnInit(): void {
    if (this.companyId) {
      this.routeForm.companyId = this.companyId;
      this.fetchExistingRoutes();
    }
  }

  fetchExistingRoutes(): void {
    this.isLoadingRoutes = true;
    this.busService.fetchAllExistingCompanyRoute(this.companyId).subscribe({
      next: (response) => {
        if (response.body) {
          this.routeList = [...response.body];
        }
        this.isLoadingRoutes = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching routes:', error);
        this.isLoadingRoutes = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * CORE LOGIC: Removes "Time per KM" stress
   * Calculates everything based on the gaps between stops
   */
  calculateJourneyMetrics(): void {
    let cumulativeMinutes = 0;
    let cumulativeDistance = 0;

    this.routeForm.stops.forEach((stop) => {
      cumulativeMinutes += (stop.minutesFromPrevious || 0);
      cumulativeDistance += (stop.distanceFromPreviousStop || 0);
      
      // Offset is now a simple sum, much more predictable for the operator
      stop.timeOffsetFromOrigin = cumulativeMinutes;
    });

    // Auto-fill the main form totals
    this.routeForm.estimatedDuration = cumulativeMinutes;
    this.routeForm.totalDistance = cumulativeDistance;
  }

  updateTotals(): void {
    this.calculateJourneyMetrics();
    this.cdr.detectChanges();
  }

  createNewRoute(): void {
    this.is_Creating = true;
    this.is_Editing = false;
    this.activeTab = 'editor';
    this.resetForm();
  }

  editRoute(route: RouteResponse): void {
    this.is_Editing = true;
    this.is_Creating = false;
    this.activeTab = 'editor';
    this.selectedRoute = route;

    // Convert stored offsets back into "minutes between" for the UI inputs
    const mappedStops = route.stops.map((stop, index, array) => {
      const prevOffset = index === 0 ? 0 : array[index - 1].timeOffsetFromOrigin;
      return {
        ...stop,
        minutesFromPrevious: stop.timeOffsetFromOrigin - prevOffset
      };
    });

    this.routeForm = {
      companyId: this.companyId,
      routeName: route.routeName,
      origin: route.origin,
      destination: route.destination,
      stops: mappedStops,
      totalDistance: route.totalDistance,
      estimatedDuration: route.estimatedDuration
    };
  }

  addStop(): void {
    const newStop: RouteStopDto = {
      stopName: '',
      distanceFromPreviousStop: 0,
      minutesFromPrevious: 0, 
      stopOrder: this.routeForm.stops.length + 1,
      timeOffsetFromOrigin: 0
    };
    this.routeForm.stops.push(newStop);
    this.updateTotals();
  }

  removeStop(index: number): void {
    if (confirm('Are you sure you want to remove this stop?')) {
      this.routeForm.stops.splice(index, 1);
      this.routeForm.stops.forEach((stop, i) => stop.stopOrder = i + 1);
      this.updateTotals();
    }
  }

  saveRoute(): void {
    if (!this.isRouteValid()) {
      alert('Please fill all required fields (Stop Names, Distances, and Times)');
      return;
    }

    this.isSavingRoute = true;
    const request = this.is_Creating 
      ? this.busService.createRouteForCompany(this.routeForm)
      : this.busService.updateRouteForCompnay(this.routeForm, this.selectedRoute!.routeId);

    request.subscribe({
      next: () => {
        this.fetchExistingRoutes();
        this.backToInventory();
        this.isSavingRoute = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error saving route:', err);
        alert('Failed to save route.');
        this.isSavingRoute = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteRoute(routeId: string): void {
    if (!confirm('Are you sure you want to delete this route?')) return;

    this.isDeletingRoute = true;
    this.busService.deleteRouteForCompany(routeId).subscribe({
      next: () => {
        this.fetchExistingRoutes();
        this.isDeletingRoute = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error deleting route:', err);
        this.isDeletingRoute = false;
      }
    });
  }

  isRouteValid(): boolean {
    const basicInfo = !!(this.routeForm.routeName && this.routeForm.origin && this.routeForm.destination);
    const hasStops = this.routeForm.stops.length > 0;
    const stopsValid = this.routeForm.stops.every(s => 
      s.stopName?.trim() !== '' && 
      s.distanceFromPreviousStop >= 0 && 
      (s.minutesFromPrevious ?? 0) >= 0
    );
    return basicInfo && hasStops && stopsValid;
  }

  backToInventory(): void {
    this.activeTab = 'inventory';
    this.resetForm();
  }

  resetForm(): void {
    this.routeForm = {
      companyId: this.companyId,
      routeName: '',
      origin: '',
      destination: '',
      stops: [],
      totalDistance: 0,
      estimatedDuration: 0
    };
    this.selectedRoute = null;
  }

  formatDuration(minutes: number): string {
    if (!minutes || minutes <= 0) return '0m';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
  }

  getStopCount(): number {
    return this.routeForm.stops.length;
  }
}