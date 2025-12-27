import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import {
  AbstractControl, FormArray, FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'app-step4-security-qas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './step4-security-qas.html',
})
export class Step4SecurityQAs {
  @Input() secQA!: FormArray;        // gets FormArray from parent
  @Input() questions!: string[];     // list of security questions

  // 🔑 helper to cast AbstractControl -> FormGroup for the template
  asFormGroup(control: AbstractControl): FormGroup {
    return control as FormGroup;
  }
}

