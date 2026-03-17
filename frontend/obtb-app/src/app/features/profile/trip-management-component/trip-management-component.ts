import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, Input, OnInit } from '@angular/core';
import { TripService } from '../../../core/services/trip-service';
import { TripInstanceDto, TripTemplateDto } from '../../../interfaces/trip-model';
import { TripSchedulerForm } from './trip-scheduler-form/trip-scheduler-form';

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
  private cdr: ChangeDetectorRef = inject(ChangeDetectorRef);

  templates: TripTemplateDto[] = [];
  upcomingJourneys: TripInstanceDto[] = [];
  isLoading = false;
  schedulerDrawerOpen: boolean = false;
  templateIds: string[] = [];

  ngOnInit(): void {
    
    if (this.companyId) this.loadDashboard();
    console.log(this.companyId);
    console.log(this.userId);
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
      next: response =>  {
        console.log('Full API Response:', response); // CHECK THIS IN CONSOLE 
        // If your API returns { status: 200, message: "...", body: [...] }
        if (response && response.body) {
          this.templates = [ ...response.body];
          console.log(this.templates[0].busId +"\t" +this.templates[0].busName);
          this.cdr.detectChanges()
        } else if (Array.isArray(response)) {
          // Fallback if the service already mapped it to the array
          this.templates = response;
        }

        console.log('Templates assigned to variable:', this.templates);

        if (this.templates.length > 0) {
          this.templateIds = this.templates.map(t => t.templateId);
          console.log("collected templateIds are:\t"+this.templateIds);
          this.loadUpcomingForRoute(this.templateIds);
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('API Error:', err);
        this.isLoading = false;
      }
    });
  }

  loadUpcomingForRoute(templateIds: string[]): void {
    this.isLoading = true;
    this.tripService.getActiveJourneys(templateIds).subscribe({
      next: response =>{
        console.log(response.body);
        this.upcomingJourneys = [ ...response.body];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.isLoading = false;
        console.error('Error fetching journeys', err)
        this.cdr.detectChanges();
      }
    });
  }

  onScheduleCreated() {
    this.closeSchedulerDrawer();
    this.loadDashboard(); 
  }

  onToggleStatus(template: TripTemplateDto): void {
    const newStatus = !template.isActive; // Corrected: isActive instead of active
    this.tripService.toggleTemplateStatus(template.templateId, newStatus).subscribe({
      next: () => template.isActive = newStatus,
      error: (err) => console.error('Status toggle failed', err)
    });
  }
}