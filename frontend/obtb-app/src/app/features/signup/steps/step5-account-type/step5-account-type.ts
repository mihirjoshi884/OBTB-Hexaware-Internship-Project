import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-step5-account-type',
  imports: [ReactiveFormsModule],
  templateUrl: './step5-account-type.html',
  standalone: true
})
export class Step5AccountType {
  @Input() form!: FormGroup;
}
