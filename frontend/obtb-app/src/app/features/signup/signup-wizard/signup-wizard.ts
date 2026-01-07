import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { environment } from '../../../../environments/environment';
import { DataStore } from '../../../core/data-store/data-store';
import { Step1PersonalInfo } from '../steps/step1-personal-info/step1-personal-info';
import { Step2Contact } from '../steps/step2-contact/step2-contact';
import { Step3Credentials } from '../steps/step3-credentials/step3-credentials';
import { Step4SecurityQAs } from '../steps/step4-security-qas/step4-security-qas';
import { Step5AccountType } from '../steps/step5-account-type/step5-account-type';

@Component({
  selector: 'app-signup-wizard',
  standalone: true,
  templateUrl: './signup-wizard.html',
  styleUrl: './signup-wizard.css',

  // ✔ FIX: Now includes CommonModule + ReactiveFormsModule + RouterLink
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    Step1PersonalInfo,
    Step2Contact,
    Step3Credentials,
    Step4SecurityQAs,
    Step5AccountType
  ]
})
export class SignupWizardComponent implements OnInit {

  currentStep = 1;

  signupForm!: FormGroup;

  securityQuestions = [
    "What is the first phone number you memorized as a child?",
    "What is the last name of your favorite high school teacher?"
  ];

  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly dataStore = inject(DataStore);
  private readonly router = inject(Router);



  ngOnInit(): void {
    this.signupForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],

      email: ['', [Validators.required, Validators.email]],
      contact: ['', Validators.required],

      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],

      SecQA: this.fb.array([
        this.createQAPair(),
        this.createQAPair()
      ]),

      roleName: ['', Validators.required]
    });
  }

  createQAPair(): FormGroup {
    return this.fb.group({
      question: ['', Validators.required],
      answer: ['', Validators.required]
    });
  }

  get secQAArray(): FormArray {
    return this.signupForm.get('SecQA') as FormArray;
  }

  nextStep() {
    if (this.currentStep < 5) this.currentStep++;
  }

  prevStep() {
    if (this.currentStep > 1) this.currentStep--;
  }

  submitSignup() {
    if (this.signupForm.valid) {
      // Show loader while processing signup
      this.dataStore.setLoading(true);

      console.log("FORM SUBMITTED:", this.signupForm.value);

      // Make API call to register user
      const signupPayload = this.signupForm.value;
      
      this.http.post( environment.baseUrls['authservice.base-uri'] + '/auth-api/v1/register', signupPayload)
        .subscribe({
          next: (response: any) => {
            console.log('✓ Signup successful:', response);
            // Hide loader
            this.dataStore.setLoading(false);
            Swal.fire({
              title:'Account Created Successfully!',
              text: 'Please verify your account and  activate your account. Check your email for verification link.',
              icon: 'success',
              confirmButtonText: 'OK',
              confirmButtonColor: '#2563EB',
            }).then((result: any)=>{
              if (result.isConfirmed){
                this.router.navigate(['/login']);
              }
            });
            
            
          },
          error: (error: any) => {
            console.error('✗ Signup failed:', error);
            // Hide loader
            this.dataStore.setLoading(false);
            // Show error message (you can add error display in template)
            alert('Signup failed. Please try again.');
          }
        });
    } else {
      console.log("Form Invalid");
    }
  }
}
