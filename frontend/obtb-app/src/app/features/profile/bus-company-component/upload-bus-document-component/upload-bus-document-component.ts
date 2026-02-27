import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BusDocumentUploadRequest } from 'src/app/interfaces/bus-operator.models';

@Component({
  selector: 'app-upload-bus-document-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload-bus-document-component.html'
})
export class UploadBusDocumentComponent {
  @Input() request!: BusDocumentUploadRequest | null;
  @Input() isUploading = false;
  @Output() fileChanged = new EventEmitter<{file: File, key: 'rcBook' | 'insurance' | 'registrationNumberPlate'}>();
  @Output() close = new EventEmitter<void>();
  @Output() upload = new EventEmitter<void>();

  onFileChange(event: Event, key: 'rcBook' | 'insurance' | 'registrationNumberPlate') {
    const element = event.target as HTMLInputElement;
    const file = element.files?.[0];
    if (file) {
      this.fileChanged.emit({ file, key });
    }
  }

  removeFile(key: 'rcBook' | 'insurance' | 'registrationNumberPlate') {
    if (this.request) {
      this.request[key] = null;
    }
  }

  onUploadDocuments(){
    this.upload.emit();
  }
}