import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-step3-credentials',
  imports: [ReactiveFormsModule],
  templateUrl: './step3-credentials.html',
  standalone: true
})
export class Step3Credentials {
  @Input() form!: FormGroup;
}


