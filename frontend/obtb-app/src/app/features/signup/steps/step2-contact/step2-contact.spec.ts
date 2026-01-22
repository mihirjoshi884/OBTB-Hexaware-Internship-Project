import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Step2Contact } from './step2-contact';

describe('Step2Contact', () => {
  let component: Step2Contact;
  let fixture: ComponentFixture<Step2Contact>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step2Contact]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Step2Contact);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
