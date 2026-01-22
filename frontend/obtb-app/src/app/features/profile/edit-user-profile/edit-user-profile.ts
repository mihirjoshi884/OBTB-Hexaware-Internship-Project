import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-edit-user-profile',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './edit-user-profile.html',
})
export class EditUserProfile {

  private fb = inject(FormBuilder);

  @Input() userData: any;
  @Output() canceled = new EventEmitter<void>();
  @Output() updated = new EventEmitter<any>();
  @Input() isSaving: boolean = false;

  localUser: any;
  selectedFile: File | null = null;
  previewUrl: string | null = null;

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
        this.selectedFile = file;
        
        // Create a local preview URL
        const reader = new FileReader();
        reader.onload = () => {
            this.previewUrl = reader.result as string;
            // Update the localUser object so the save() method has the new URL
            this.localUser.profilePictureUrl = this.previewUrl;
        };
        reader.readAsDataURL(file);
    }
  }
  ngOnInit() {
    this.localUser = { ...this.userData };
    console.log('Child initialized with local copy:', this.localUser);
  }

  save() {
    // Wrap both pieces of information into one object
    const eventPayload = {
      data: this.localUser,
      file: this.selectedFile
    };
    
    console.log('Child emitting payload:', eventPayload);
    this.updated.emit(eventPayload);
  }

  cancel() {
    this.canceled.emit();
  }

}
