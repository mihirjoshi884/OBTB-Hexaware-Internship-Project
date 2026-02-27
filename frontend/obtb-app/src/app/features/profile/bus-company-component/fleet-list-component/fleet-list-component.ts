import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, Input, NO_ERRORS_SCHEMA, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { forkJoin } from 'rxjs';
import { BusService } from 'src/app/core/services/bus-service';
import {
  AddBusStaffRequest,
  BusCreationResponse,
  BusDocumentUploadRequest,
  BusDocumentUploadResponse,
  BusFleetResponse,
  BusStaffResponse,
  VerificationStatus
} from 'src/app/interfaces/bus-operator.models';
import { ManageStaffModalComponent } from '../../bus-staff-component/manage-staff-modal';
import { BusLayoutPreviewComponent } from '../bus-layout-preview.component/bus-layout-preview.component';
import { UploadBusDocumentComponent } from '../upload-bus-document-component/upload-bus-document-component';

@Component({
  selector: 'app-fleet-list-component',
  standalone: true,
  imports: [CommonModule, ManageStaffModalComponent, BusLayoutPreviewComponent, UploadBusDocumentComponent,PdfViewerModule],
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

  // Staff & bus management drawer
  selectedBusForEdit: BusFleetResponse | null = null;
  existingBusDocuments: BusDocumentUploadResponse | null = null;
  availableCompanyStaff: BusStaffResponse[] = [];
  currentBusStaff: AddBusStaffRequest[] = [];
  staffDrawerOpen = false;

  onEditBus(busId: string) {
    const foundBus = this.buses.find(b => b.busId === busId);
    if (foundBus) {
        // Deep clone or re-assign to trigger a fresh reference
        this.selectedBusForEdit = { ...foundBus };
        console.log(this.selectedBusForEdit?.template?.layoutData) 
        this.staffDrawerOpen = true;
        this.fetchCompanyStaff();
        this.fetchExistingBusDocuments(busId);
        // Let the cycle finish then detect
        setTimeout(() => {
            this.cdr.detectChanges();
        }, 0);
    }
  }

  fetchExistingBusDocuments(busId: string) {
    this.busService.fetchBusDocument(busId).subscribe({
      next: (response) => {
        // response.body.documents contains List<BusDocumentResponse> (docId, docName)
        this.existingBusDocuments = response.body;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error fetching docs:', err)
    });
  }

  deleteBusDocs() {
    if (!this.selectedBusForEdit || !confirm('Delete all legal documents for this bus?')) return;

    this.busService.deleteBusDocument(this.selectedBusForEdit.busId).subscribe({
        next: () => {
            alert('Documents deleted successfully');
            this.existingBusDocuments = null;
            this.fetchBusFleet(); // Refresh status badge in list
            this.cdr.detectChanges();
        },
        error: (err) => alert('Failed to delete documents')
    });
  }

  fetchCompanyStaff(): void {
    if (!this.companyId) return;

    this.busService.fetchCompanyStaff(this.companyId).subscribe({
        next: response => {
            this.availableCompanyStaff = [...(response.body || [])];

            // ✅ Derive currentBusStaff from the fetched list
            if (this.selectedBusForEdit) {
                this.currentBusStaff = this.availableCompanyStaff
                    .filter(s => s.busId === this.selectedBusForEdit!.busId && s.dutyType !== null)
                    .map(s => ({
                        staffId: s.staffId,
                        staffName: s.staffName,
                        busId: s.busId!,
                        dutyType: s.dutyType!
                    }));
            }

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

    this.loading = true;
    const updateRequests = assignments.map(staff => this.busService.updateBusStaff(staff));

    forkJoin(updateRequests).subscribe({
        next: (results) => {
            console.log('All staff updated successfully', results);
            this.loading = false;
            this.fetchBusFleet();
            this.closeStaffDrawer();
        },
        error: (err) => {
            console.error('Error updating staff:', err);
            this.loading = false;
        }
    });
  }

  getStatusBadge(bus: BusFleetResponse): { text: string; color: string } {
    
    switch (bus.status) {
    case VerificationStatus.VERIFIED:
      return { 
        text: 'VERIFIED', 
        color: 'bg-green-600' 
      };

    case VerificationStatus.PENDING:
      return { 
        text: 'PENDING', 
        color: 'bg-blue-600' 
      };

    case VerificationStatus.REJECTED:
      return { 
        text: 'REJECTED', 
        color: 'bg-red-600' 
      };

    case VerificationStatus.NOT_SUBMITTED:
    default:
      return { 
        text: 'NO DOCUMENTS', 
        color: 'bg-yellow-600' 
      };
    }
  }

  getBusLayoutData(): any {
    if (this.selectedBusForEdit && this.selectedBusForEdit.template) {
      // Return the layoutData string from the template
      return this.selectedBusForEdit.template.layoutData;
    }
    else if(this.selectedBusForUploadDocument && this.selectedBusForUploadDocument.template){
      return this.selectedBusForUploadDocument.template.layoutData;
    }
    return null;
  }

  //Upload Document Drawer
  uploadRequest: BusDocumentUploadRequest | null = null;
  selectedBusForUploadDocument: BusFleetResponse | null = null;
  uploadDocumentDrawer = false;
  isDocumentUploading = false;

  openUploadDrawer(bus: BusFleetResponse){
    this.selectedBusForUploadDocument = bus;
    this.uploadRequest = {
      ownerId: this.userId,
      companyId: this.selectedBusForUploadDocument.company.companyId || "",
      busId: this.selectedBusForUploadDocument.busId,
      rcBook: null,
      insurance: null,
      registrationNumberPlate: null
    };
    this.uploadDocumentDrawer = true;
    this.cdr.detectChanges();
  }

  onFileSelected(file: File, key: 'rcBook' | 'insurance' | 'registrationNumberPlate') {
    if (this.uploadRequest) {
      this.uploadRequest[key] = file;
    }
    this.cdr.detectChanges();
  }

  closeUploadDrawer(): void {
    this.uploadDocumentDrawer = false;
    this.selectedBusForUploadDocument = null;
    this.uploadRequest = null;
    this.cdr.detectChanges();
  }


  // fleet-list-component.ts
  uploadBusDocuments() {
    if (!this.uploadRequest) return;

    // 1. Start the loader correctly
    this.isDocumentUploading = true;
    this.cdr.detectChanges();

    this.busService.uploadBusDocuments(this.uploadRequest).subscribe({
      next: (response) => {
        // Note: Based on your Java Record, the response has a 'documents' list
        console.log('Upload complete!', response.body.insurancePolicy, response.body.rcBook, response.body.registrationNumberPlate);
        
        alert('Documents uploaded successfully!');
        
        // Refresh and UI Cleanup
        this.fetchBusFleet(); 
        this.closeUploadDrawer();
        
        // Stop loader on success
        this.isDocumentUploading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Upload failed', err);
        alert('Failed to upload documents. Please try again.');
        
        // Stop loader on error
        this.isDocumentUploading = false;
        this.cdr.detectChanges();
      }
    });
  }

}