import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import { BusCreationResponse, CompanyCreationRequest, CompanyCreationResponse } from 'src/app/interfaces/bus-operator.models';
import { BusCreationComponentComponent } from './bus-creation-component/bus-creation-component.component';
import { BusTemplateComponentComponent } from './bus-template-component/bus-template-component.component';
import { FleetListComponent } from './fleet-list-component/fleet-list-component';



@Component({
  selector: 'app-bus-company-component',
  imports: [FormsModule, BusTemplateComponentComponent, BusCreationComponentComponent, FleetListComponent],
  templateUrl: './bus-company-component.html'
})
export class BusCompanyComponent implements OnInit{

  constructor(
    private readonly busService: BusService,
    private readonly cdr: ChangeDetectorRef
  ) { }
  @Input() userId!: string;
  @Output() companyLoaded = new EventEmitter<string>();  
  createdBus: BusCreationResponse | null = null;
  currentStep = 1;
  companyName = '';
  ownerName = '';
  companyLoading: boolean = false; 
  currentAction: null | 'createTemplate' | 'addBus' = null;
  companyResponse: CompanyCreationResponse | null = null ;
 


  ngOnInit(){
    this.companyLoading = true;
    this.fetchCompany(this.userId);
  }
  registerCompany(){
    if(!this.companyName.trim() || !this.ownerName.trim()){
      console.log("can't be empty");
      return;

    }else{
      this.companyLoading = true;
      const companyRequest: CompanyCreationRequest = {
        companyName: this.companyName,
        ownerName: this.ownerName,
        ownerId: this.userId
      }
      this.busService.createCompany(companyRequest).subscribe({
        next: response => {
          this.companyResponse = response.body;
          if(this.companyResponse?.companyId){
            this.companyLoaded.emit(this.companyResponse.companyId)
          }
          this.companyLoading = false;
          this.currentStep = 2;
          this.cdr.detectChanges()
        },
        error: error =>{
          this.companyLoading = false;
          console.error(error);
          this.cdr.detectChanges();
        }
      });
    }
  }

  fetchCompany(userId: string){
    this.busService.fetchExistingCompany(userId).subscribe({
      next: response =>{
        this.companyResponse = response.body;
        if (this.companyResponse?.companyId) {
          this.companyLoaded.emit(this.companyResponse.companyId);
        }
        this.companyLoading = false;
        this.cdr.detectChanges()
      },
      error: error =>{
        this.companyLoading = false;
        if (error.status === 404) {
          this.companyResponse = null;
          this.cdr.detectChanges();
        } else {
          console.error(error);
          
        }
      }
    });
  }
}
