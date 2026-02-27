import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { BusService } from 'src/app/core/services/bus-service';
import { DocumentResponse, DocumentUploadRequest, DocumentUploadResponse } from 'src/app/interfaces/bus-operator.models';

@Component({
  selector: 'app-upload-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, PdfViewerModule],
  templateUrl: './upload-documents.html',

})
export class UploadDocuments {

  @Input() userId: any;
  aadharNumber: string = '';
  panNumber: string = ''; 
  aadharCard: File | null = null;
  panCard: File | null = null;
  private readonly cdr = inject(ChangeDetectorRef);
  
  uploadResult: DocumentUploadResponse | null = null;
  fetchDocumentResults: DocumentResponse | null = null; 

  constructor(
    private readonly busService: BusService
  ) {}

  ngOnInit(): void {
    if(this.userId){
      this.fetchExistingDocuments();
    }
  }

  fetchExistingDocuments() {
    this.isProcessing = true;
    this.busService.fetchExistingDocuments(this.userId).subscribe({
      next: (response) => {
        if (response?.body) {
          this.fetchDocumentResults = response.body; 
          
          // Map fetchDocumentResults to uploadResult to trigger the dashboard
          this.uploadResult = {
            ...response.body,
            verificationAt: response.body.verificationAt
          } as any; 
          
          this.aadharNumber = response.body.aadharNumber || '';
          this.panNumber = response.body.panNumber || '';
        }
        this.isProcessing = false; 
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.isProcessing = false;
        this.uploadResult = null;
        this.cdr.detectChanges();
      }
    });
  }
  onAadharSelected(event: any) {
    // This takes the first file selected by the user
    this.aadharCard = event.target.files[0];
  }
  onPanSelected(event: any){
    this.panCard = event.target.files[0];
  }
  isProcessing: boolean = false;
  submitDocument(){
    if (!this.aadharCard || !this.panCard) {
      alert("Please select both files first!");
      return;
    }
    const data: DocumentUploadRequest = {
      userId: this.userId,
      aadharNumber: this.aadharNumber,
      panNumber: this.panNumber
    };
    this.isProcessing = true;
    this.busService.uploadDocuments(data, this.aadharCard, this.panCard).subscribe({
      next: (response)=>{
        console.log("Uploaded successfully!", response);
        this.uploadResult = response.body;
        this.isProcessing = false;
        this.cdr.detectChanges();
      },
      error: (error)=>{
        console.error("Upload failed", error);
      }
    })
  }
  prepareUpdate() {
    if (confirm("This will allow you to select new files to update your records. Continue?")) {
      this.uploadResult = null; 
      this.cdr.detectChanges();
    }
  }

  deleteDocuments() {
    if (!confirm("Are you sure you want to delete these document records? This action cannot be undone.")) {
      return;
    }

    this.isProcessing = true;
    this.busService.deleteOperatorDocuments(this.userId).subscribe({
      next: () => {
        alert("Documents deleted successfully");
        this.resetForm(); // Completely clears the state
        this.isProcessing = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error("Delete failed", error);
        alert("Failed to delete documents. Please try again.");
        this.isProcessing = false;
        this.cdr.detectChanges();
      }
    });
  }
  resetForm() {
    this.uploadResult = null;
    this.aadharNumber = '';
    this.panNumber = '';
    this.aadharCard = null;
    this.panCard = null;
    this.cdr.detectChanges();
  }
}
