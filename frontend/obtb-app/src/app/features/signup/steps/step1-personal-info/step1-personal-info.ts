import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-step1-personal-info',
  imports: [ReactiveFormsModule],
  templateUrl: './step1-personal-info.html',
  standalone: true
})
export class Step1PersonalInfo {
  @Input() form!: FormGroup;
}
