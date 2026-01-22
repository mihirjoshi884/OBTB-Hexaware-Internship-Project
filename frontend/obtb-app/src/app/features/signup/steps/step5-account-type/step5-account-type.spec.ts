import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Step5AccountType } from './step5-account-type';

describe('Step5AccountType', () => {
  let component: Step5AccountType;
  let fixture: ComponentFixture<Step5AccountType>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step5AccountType]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Step5AccountType);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
