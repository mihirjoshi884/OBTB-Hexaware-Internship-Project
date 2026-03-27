
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SearchService } from 'src/app/core/services/search-service';
import { JourneyType, SearchRequestDto, TripSearchResponseDto } from 'src/app/interfaces/search-interface';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-bus-search-engine',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bus-search-engine.html'
})
export class BusSearchEngine {

  constructor(
    private readonly authService: AuthService,
    private readonly searchService: SearchService,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router
  ){}

  public JourneyType = JourneyType;


  isSearchPerformed: boolean = false;
  isSearching: boolean = false;
  moveToBooking: boolean = false;

  // Form state
  selectedJourneyType: JourneyType | "" = JourneyType.ONE_WAY;
  selectedSourceLocation = '';
  selectedDestinationLocation = '';
  selectedDepartureDate = '';
  selectedReturnDate = '';
  departureBuses: TripSearchResponseDto [] | null = null;
  returnBuses: TripSearchResponseDto [] | null = null;
  selectedBus: TripSearchResponseDto | null = null;
  


  onSelectJourneyType(event: Event): void{
    const val = this.getElementValue(event);
    this.selectedJourneyType = val as JourneyType;
  }

  onSelectSourceLocation(event: Event): void{
    this.selectedSourceLocation = this.getElementValue(event);
  }

  onSelectDestinationLocation(event: Event): void{
    this.selectedDestinationLocation = this.getElementValue(event);
  }

  onSelectDepartureDate(event: Event): void{
    this.selectedDepartureDate = this.getElementValue(event);
  }

  onSelectReturnDate(event: Event): void{
    this.selectedReturnDate = this.getElementValue(event);
  }

  onSelectBus(bus: TripSearchResponseDto): void {
    this.selectedBus = bus;
    sessionStorage.setItem('selected_bus_cache', JSON.stringify(bus));
    this.router.navigate(['/booking', bus.instanceId]);
  }


  constructSearchRequest(): SearchRequestDto {
    const request: SearchRequestDto = {
      source: this.selectedSourceLocation,
      destination: this.selectedDestinationLocation,
      departureDate: this.selectedDepartureDate,
      journeyType: this.selectedJourneyType as JourneyType,
      returnDate: this.selectedReturnDate
    }

    return request;
  }
  searchBuses(): void {
    
    this.isSearching = true;
    const request:SearchRequestDto = this.constructSearchRequest();
    console.log(request); 
    this.searchService.findBusesInstances(request).subscribe({
      next: response => {
        console.log("Full Response:", response.body); 
        // If your backend DTO has a field called 'body':
        if (response && response.body && response.body.length > 0) {
          this.departureBuses = response.body.filter(bus => 
            bus.direction?.toUpperCase() === 'OUTBOUND'
          );
          this.returnBuses = response.body.filter(bus => 
            bus.direction?.toUpperCase() === 'INBOUND'
          );
          this.isSearchPerformed = true;
          console.log("Outbound Count:", this.departureBuses.length);
          console.log("Inbound Count:", this.returnBuses.length);
        } else {
          // 3. Logic when data is NOT present (empty array)
          this.departureBuses = [];
          this.returnBuses = [];
          console.warn("No buses found for this route.");
        }
        this.isSearching = false;
        this.cdr.detectChanges();
      },
      error: error => {
        console.error("Error occurred:", error);
        alert("Error occurred:"+ error);
        this.isSearching = false;
        this.cdr.detectChanges();

      }
    });
  }

  resetSearch(): void {
    this.selectedJourneyType = JourneyType.ONE_WAY;
    this.selectedSourceLocation = '';
    this.selectedDestinationLocation = '';
    this.selectedDepartureDate = '';
    this.selectedReturnDate = '';
  }

   private getElementValue(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement | null)?.value ?? '';
  }

}
