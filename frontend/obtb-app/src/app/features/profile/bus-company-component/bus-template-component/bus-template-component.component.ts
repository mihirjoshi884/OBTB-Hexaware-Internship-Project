import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusService } from 'src/app/core/services/bus-service';
import { BusTemplateCreationRequest, LayoutLookupResponse } from 'src/app/interfaces/bus-operator.models';

@Component({
  selector: 'app-bus-template-component',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './bus-template-component.component.html'
})
export class BusTemplateComponentComponent implements OnInit{

  @Output() back = new EventEmitter<void> ();

  TemplateName: string = '';
  totalSeats: number = 0;
  selectedLayout: LayoutLookupResponse | null  = null;
  dropdownOpen = false;

  isSubmitting = false;
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

  selectedBusType: any = null;
  busTypeDropdownOpen = false;


  ngOnInit(): void {
      this.fetchLayout();
  }
  notifyParent() {
    this.back.emit();
  }


  layoutLookUpResponse: LayoutLookupResponse[] = [];

  constructor (private readonly busService: BusService, private readonly cdr: ChangeDetectorRef){}

  fetchLayout(){
    this.busService.fetchLayoutTemplates().subscribe({
      next: response =>{
        this.layoutLookUpResponse = Array.isArray(response.body) ? response.body : [];
      },
      error: error =>{
        console.error(error)
      }
    })
  }

  selectLayout(layout: LayoutLookupResponse) {
    this.selectedLayout = layout;
    this.dropdownOpen = false;
  }

  selectBusType(type: any) {
    this.selectedBusType = type;
    this.busTypeDropdownOpen = false;
  }

  createBusTemplate() {
    if (!this.selectedLayout?.layoutId) {
        console.error('Layout must be selected');
        return;
    }

    this.isSubmitting = true;
    this.submitSuccess = null;
    this.submitError = null;

    const requestData: BusTemplateCreationRequest = {
        templateName: this.TemplateName,
        layoutId: this.selectedLayout.layoutId,
        totalSeats: this.totalSeats,
        busType: this.selectedBusType.value
    };

    this.busService.createBusTemplate(requestData).subscribe({
        next: response => {
            // Log safely using optional chaining to avoid TypeErrors
            console.log('Message:', response?.message);
            console.log('Status:', response?.status);
            
            if (response?.body?.templateId) {
                console.log('Template ID:', response.body.templateId);
            }

            this.isSubmitting = false;
            this.submitSuccess = response?.message || 'Template created successfully';
            this.cdr.detectChanges();
        },
        error: error => {
            console.error(error);
            this.isSubmitting = false;
            this.submitError = error?.error?.message || 'Failed to create template';
            this.cdr.detectChanges();
        }
    });
  }
  resetForm(){
    this.TemplateName = '';
    this.totalSeats = 0;
    this.selectedLayout = null;
    this.selectedBusType = null;
    this.submitSuccess = null;
    this.submitError = null;
  }
}
