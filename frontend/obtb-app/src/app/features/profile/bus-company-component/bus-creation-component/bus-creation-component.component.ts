import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import { BusCreationRequest, BusFleetResponse, BusTemplate, ResponseDto } from 'src/app/interfaces/bus-operator.models';
import { BusLayoutPreviewComponent } from '../bus-layout-preview.component/bus-layout-preview.component';

@Component({
  selector: 'app-bus-creation-component',
  standalone: true,
  imports: [FormsModule, BusLayoutPreviewComponent],
  templateUrl: './bus-creation-component.component.html'
})
export class BusCreationComponentComponent implements OnInit{

  constructor (
    private readonly busService: BusService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  busName: string = "";
  regNumber: number | null= null;
  IP_Number: number | null = null;
  RC_Number: number | null = null;
  dropdownOpen = false;
  selectedBusTemplate: BusTemplate |null = null;
  parsedSeats: any[] = [];
  validationError: string | null = null;
  submitSuccess: string | null = null;
  submitError: string | null = null;
  busTypes = [
    { value: 'AC_SEATER', label: 'AC Seater' },
    { value: 'AC_SLEEPER', label: 'AC Sleeper' },
    { value: 'AC_HYBRID', label: 'AC Hybrid' },
    { value: 'NON_AC_SEATER', label: 'Non-AC Seater' },
    { value: 'NON_AC_SLEEPER', label: 'Non-AC Sleeper' },
    { value: 'NON_AC_HYBRID', label: 'Non-AC Hybrid' }
  ];
  isCreating = false;
  selectedBusType: any = null;
  busTypeDropdownOpen = false;

  @Output() back = new EventEmitter<void>();
  @Output() createdBus = new EventEmitter<BusFleetResponse>();
  @Input() userId!: string;
  @Input() companyId!: string | undefined;
  busTemplates: BusTemplate [] = [];
  isBusTemplateLoading = false;

  ngOnInit(): void {
      this.fetchBusTemplates(this.userId);

  }

  notifyParent() {
    this.back.emit();
  }

  fetchBusTemplates(userId: string){
    this.busService.fetchBusTemplate(userId).subscribe({
      next: (response: ResponseDto<BusTemplate[]>) =>{
        this.busTemplates = Array.isArray(response.body) ? response.body: [];
      },
      error: (error: HttpErrorResponse) => {
        console.error(error);
      }
    });
  }

  
  selectBusTemplate(template: BusTemplate) {
    this.selectedBusTemplate = template;
    this.dropdownOpen = false;
    this.validateBusType();
  }

  get gridColumns() {
    if (!this.parsedSeats.length) return 4;
    return Math.max(...this.parsedSeats.map(s => s.x_coordinate)) + 1;
  }

  validateBusType() {
    // Reset error first
    this.validationError = null;

    if (this.selectedBusTemplate && this.selectedBusType) {
        // Compare the template's busType (e.g., "AC_SLEEPER") with the user selection
        //
        if (this.selectedBusTemplate.busType !== this.selectedBusType.value) {
            this.validationError = `Warning: The chosen template is for ${this.selectedBusTemplate.busType}, but you selected ${this.selectedBusType.label}.`;
        }
    }
  }

  selectBusType(type: { label: string, value: string }) {
    this.selectedBusType = type;
    this.busTypeDropdownOpen = false;
    this.validateBusType();
  }

  private runTypeValidation() {
    this.validationError = null;

    if (this.selectedBusTemplate && this.selectedBusType) {
      // Compare Backend Enum string with selected dropdown value
      if (this.selectedBusTemplate.busType !== this.selectedBusType.value) {
        this.validationError = `Mismatched Type: Selected template is ${this.selectedBusTemplate.busType.replace('_', ' ')}.`;
      }
    }
  }

  createBus(){
    if(this.isCreating) return;

    this.isCreating = true;
    this.submitSuccess = null;
    this.submitError = null;

    const requestData: BusCreationRequest = {
      busName: this.busName,
      busType: this.selectedBusType.value,
      companyId: this.companyId || '',
      templateId: this.selectedBusTemplate?.templateId || '',
      registrationNumber: this.regNumber?.toString() || '',
      rcNumber: this.RC_Number?.toString() || '',
      insurancePolicyNumber: this.IP_Number?.toString() || '',
      
    }
    this.busService.createBus(requestData).subscribe({
      next: (response: ResponseDto<BusFleetResponse>) =>{
        this.isCreating = false;
        this.submitSuccess = response?.message || 'Bus created successfully!';
        this.createdBus.emit(response.body)
        this.cdr.detectChanges()
      },
      error: (error: HttpErrorResponse) =>{
        this.isCreating = false;
        this.submitError = error?.error?.message || 'Failed to create bus';
        console.error(error)
        this.cdr.detectChanges();
      }
    })

  }

  resetForm(){
    this.busName = '';
    this.regNumber = null;
    this.IP_Number = null;
    this.RC_Number = null;
    this.selectedBusTemplate = null;
    this.selectedBusType = null;
    this.validationError = null;
    this.submitSuccess = null;
    this.submitError = null;
  }

}
