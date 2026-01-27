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
  private cdr = inject(ChangeDetectorRef);
  
  uploadResult: DocumentUploadResponse | null = null;
  fetchDocumentResults: DocumentResponse | null = null; 

  constructor(
    private busService: BusService
  ) {}

  ngOnInit(){
    if(this.userId){
      this.fetchExistingDocuments();
    }
  }

  fetchExistingDocuments() {
    this.isProcessing = true;
    this.busService.fetchExistingDocuments(this.userId).subscribe({
      next: (response) => {
        if (response && response.body) {
          this.fetchDocumentResults = response.body; 
          
          // Map fetchDocumentResults to uploadResult to trigger the dashboard
          // We ensure 'verification' is mapped to 'verificationAt' if needed
          this.uploadResult = {
            ...response.body,
            verificationAt: response.body.verification // Syncing the date field
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
    this.busService.uploadDocuments(data, this.aadharCard!, this.panCard!).subscribe({
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
  resetForm() {
  // 1. Hide the "Documents Submitted" dashboard and unlock the form
    this.uploadResult = null;

    // 2. Clear the text data
    this.aadharNumber = '';
    this.panNumber = '';

    // 3. Clear the file references
    this.aadharCard = null;
    this.panCard = null;

    // 4. Force UI refresh
    this.cdr.detectChanges();
  }
}
