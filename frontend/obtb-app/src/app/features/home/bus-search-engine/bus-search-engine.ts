
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SearchService } from 'src/app/core/services/search-service';
import { JourneyType, SearchRequestDto } from 'src/app/interfaces/search-interface';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-bus-search-engine',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bus-search-engine.html'
})
export class BusSearchEngine {

  constructor(
    private readonly authService: AuthService,
    private readonly searchService: SearchService
  ){}

  public JourneyType = JourneyType;


  // Form state
  selectedJourneyType: JourneyType | "" = JourneyType.ONE_WAY;
  selectedSourceLocation = '';
  selectedDestinationLocation = '';
  selectedDepartureDate = '';
  selectedReturnDate = '';

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
    
    const request:SearchRequestDto = this.constructSearchRequest();
    console.log(request); 
    this.searchService.findBusesInstances(request).subscribe({
      next: response => {
        console.log("Full Response:", response); 
        // If your backend DTO has a field called 'body':
        if (response && response.body) {
            console.log("Data:", response.body);
        }
      },
      error: error => {
        console.error("Error occurred:", error);
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
