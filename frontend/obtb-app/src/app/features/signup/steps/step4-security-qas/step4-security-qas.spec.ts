import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Step4SecurityQas } from './step4-security-qas';

describe('Step4SecurityQas', () => {
  let component: Step4SecurityQas;
  let fixture: ComponentFixture<Step4SecurityQas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step4SecurityQas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Step4SecurityQas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
