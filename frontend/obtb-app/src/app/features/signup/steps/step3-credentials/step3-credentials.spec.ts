import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Step3Credentials } from './step3-credentials';

describe('Step3Credentials', () => {
  let component: Step3Credentials;
  let fixture: ComponentFixture<Step3Credentials>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step3Credentials]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Step3Credentials);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
