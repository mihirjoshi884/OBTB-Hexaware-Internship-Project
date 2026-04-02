import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PrimaryPassangerDetailCreationRequest } from 'src/app/interfaces/booking-interfaces';

@Component({
  selector: 'app-primary-passanger-form-component',
  standalone: true,
  imports: [CommonModule, FormsModule], // Required for [(ngModel)]
  templateUrl: './primary-passanger-form-component.html',
})
export class PrimaryPassangerFormComponent {
  // Input received from Parent
  @Input() profile!: PrimaryPassangerDetailCreationRequest;
  
  // Event emitted back to Parent
  @Output() profileCreated = new EventEmitter<PrimaryPassangerDetailCreationRequest>();

  onSubmit() {
    // Pass the filled-out profile object up to the parent component
    this.profileCreated.emit(this.profile);
  }
}
