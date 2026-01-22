import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-step2-contact',
  imports: [ReactiveFormsModule],
  templateUrl: './step2-contact.html',
  standalone: true
})
export class Step2Contact {
  @Input() form!: FormGroup;
}

