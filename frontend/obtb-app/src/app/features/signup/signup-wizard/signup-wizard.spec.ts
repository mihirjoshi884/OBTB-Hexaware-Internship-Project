import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignupWizard } from './signup-wizard';

describe('SignupWizard', () => {
  let component: SignupWizard;
  let fixture: ComponentFixture<SignupWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignupWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignupWizard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
