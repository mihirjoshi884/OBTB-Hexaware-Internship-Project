import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Step1PersonalInfo } from './step1-personal-info';

describe('Step1PersonalInfo', () => {
  let component: Step1PersonalInfo;
  let fixture: ComponentFixture<Step1PersonalInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step1PersonalInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Step1PersonalInfo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
