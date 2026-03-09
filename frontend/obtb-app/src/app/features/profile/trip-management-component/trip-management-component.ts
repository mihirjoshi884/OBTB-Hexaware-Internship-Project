import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TripSchedulerForm } from './trip-scheduler-form/trip-scheduler-form';
import { TripService } from '../../../core/services/trip-service'; 
import { tripDetail } from '../../../interfaces/trip-model';

@Component({
  selector: 'app-trip-management-component',
  standalone: true,
  imports: [CommonModule, TripSchedulerForm],
  templateUrl: './trip-management-component.html'
})
export class TripManagementComponent implements OnInit {
  @Input() companyId: string = '';
  @Input() userId: string = '';

  private tripService: TripService = inject(TripService);

  templates: any[] = [];
  upcomingJourneys: tripDetail[] = [];
  isLoading = false;
  schedulerDrawerOpen: boolean = false;

  ngOnInit(): void {
    if (this.companyId) this.loadDashboard();
  }

  // CHANGED: Renamed to match the (click) event in your HTML
  openCreateTripModal() {
    this.schedulerDrawerOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeSchedulerDrawer() {
    this.schedulerDrawerOpen = false;
    document.body.style.overflow = 'auto';
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.tripService.getMyTemplates(this.companyId).subscribe({
      next: (data: any[]) => {
        this.templates = data || [];
        if (this.templates.length > 0 && this.templates[0].routeId) {
          this.loadUpcomingForRoute(this.templates[0].routeId);
        }
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Error fetching templates', err);
        this.isLoading = false;
      }
    });
  }

  loadUpcomingForRoute(routeId: string): void {
    this.tripService.getActiveJourneys(routeId).subscribe({
      next: (journeys: tripDetail[]) => this.upcomingJourneys = journeys,
      error: (err: any) => console.error('Error fetching journeys', err)
    });
  }

  onScheduleCreated() {
    this.closeSchedulerDrawer();
    this.loadDashboard(); 
  }

  onToggleStatus(template: any): void {
    const newStatus = !template.active;
    this.tripService.toggleTemplateStatus(template.templateId, newStatus).subscribe({
      next: () => template.active = newStatus
    });
  }
}