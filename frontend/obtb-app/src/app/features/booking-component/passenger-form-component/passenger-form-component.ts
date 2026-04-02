import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EnrichedSeatDto, PassengerDetailDto } from 'src/app/interfaces/booking-interfaces';

@Component({
  selector: 'app-passenger-form-component',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './passenger-form-component.html'
})
export class PassengerFormComponent implements OnInit {

  @Input() selectedSeats?: EnrichedSeatDto[] | null = null;
  @Input() loading: boolean = false;
  
  // Updated Output to emit both the JSON data and the corresponding ID files
  @Output() passengersSubmitted = new EventEmitter<{ passengers: PassengerDetailDto[], files: File[] }>();

  passengerForm!: FormGroup;
  
  // Independent array to store raw files mapping to passenger indices
  selectedFiles: File[] = [];

  constructor(private readonly fb: FormBuilder) {}

  ngOnInit() {
    this.passengerForm = this.fb.group({
      passengers: this.fb.array([])
    });

    this.initializeForm();
  }

  get passengers(): FormArray {
    return this.passengerForm.get('passengers') as FormArray;
  }

  private initializeForm() {
    if (this.selectedSeats) {
      this.selectedSeats.forEach(seat => {
        this.passengers.push(this.fb.group({
          seatNumber: [seat.seatNumber],
          name: ['', [Validators.required, Validators.minLength(3)]],
          age: ['', [Validators.required, Validators.min(1), Validators.max(120)]],
          gender: ['', Validators.required],
          idType: ['', Validators.required],
          idNumber: ['', [Validators.required, Validators.minLength(5)]]
        }));
        
        // Push a null placeholder initially for each passenger's file
        this.selectedFiles.push(null as any); 
      });
    }
  }

  onFileChange(event: any, index: number) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFiles[index] = file;
    }
  }

  onSubmit() {
    // Check if form is valid and EVERY passenger has uploaded a file
    const filesAreComplete = this.selectedFiles.every(file => file !== null);

    if (this.passengerForm.valid && filesAreComplete) {
      this.passengersSubmitted.emit({
        passengers: this.passengerForm.value.passengers,
        files: this.selectedFiles
      });
    } else if (!filesAreComplete) {
      alert('Please upload ID proofs for all passengers.');
    }
  }
}