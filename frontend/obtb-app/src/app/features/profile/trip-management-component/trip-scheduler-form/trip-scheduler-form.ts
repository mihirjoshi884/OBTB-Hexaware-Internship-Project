import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusFleetResponse, RouteResponse } from 'src/app/interfaces/bus-operator.models';
import { BusService } from '../../../../core/services/bus-service';
import { TripService } from '../../../../core/services/trip-service';
import { tripCreationRequest, TripType } from '../../../../interfaces/trip-model';


@Component({
  selector: 'app-trip-scheduler-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [BusService, TripService],
  templateUrl: './trip-scheduler-form.html'
})
export class TripSchedulerForm implements OnInit{

  @Input() companyId: string = '';
  @Output() onSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  buses: BusFleetResponse[] | null = null;
  routes: RouteResponse[]| null = null;

  is_busLoading = false;
  is_busRoutesLoading = false;

  selectedBusId: string  = "";
  selectedRouteId: string = "";
  public TripType = TripType;
  tripTypes = Object.values(TripType);
  selectedTripType: TripType  = TripType.ONE_TIME;
  selectedDay: string = "MONDAY";
  daysOfWeek: string[] = ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"]; 

  constructor (
    private readonly tripService: TripService,
    private readonly busService: BusService,
    private readonly cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    this.fetchBuses(this.companyId);
    this.fetchRoutes(this.companyId);
  }

  onBusSelect(event: any): void {
    this.selectedBusId = event.target.value;
    console.log("selected bus id is: \n"+this.selectedBusId); 
  }

  onRouteSelect(event:any):void{
    this.selectedRouteId = event.target.value; 
    console.log("selected route id  is:\n"+this.selectedRouteId);
  }

  onDaySelect(event:any):void {
    this.selectedDay =  event.target.value; 
    console.log("selected day is:\t"+this.selectedDay);
  }

  onTripTypeSelect(event:any):void {
    this.selectedTripType = event.target.value;
    console.log("selected trip type is:\t"+this.selectedTripType);
  }

  fetchBuses(companyId: string){
    if(companyId!=null || ''){
        console.log("companyId is:"+companyId )
        this.is_busLoading = true;
        this.busService.fetchBuses(companyId).subscribe({
          next: response=> {
            this.is_busLoading = false;
            this.buses = [ ...response.body];
            console.log(this.buses);
          },
          error: error=> {
            this.is_busLoading = false;
            console.error("something went wrong:\t"+error );
          }
        })
    }
  }

  fetchRoutes(compnayId: string){
    if(this.companyId!=null || ''){
      this.is_busRoutesLoading = true;
      this.busService.fetchAllExistingCompanyRoute(compnayId).subscribe({
        next: response=>{
          this.is_busRoutesLoading = false;
          this.routes = [ ...response.body];
          console.log(this.routes);
        },
        error: error=>{
          this.is_busRoutesLoading = false;
          console.error("something went wrong:"+error);
        }
      })
    }
  }

  // Initialize with Date objects to satisfy the interface
  request: tripCreationRequest = {
    routeId: this.selectedRouteId,
    busId: this.selectedBusId,
    companyId: this.companyId,
    tripType: this.selectedTripType, 
    
    departureTime: new Date(),
    arrivalTime: new Date(),
    scheduledDay: this.selectedDay,
    regularDepartureTime: new Date,
    baseFare: 0
  };
  onDateChange(event: string, field: 'departureTime' | 'arrivalTime') {
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