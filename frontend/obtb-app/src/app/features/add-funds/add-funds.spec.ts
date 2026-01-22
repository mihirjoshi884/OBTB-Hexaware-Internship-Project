import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddFunds } from './add-funds';

describe('AddFunds', () => {
  let component: AddFunds;
  let fixture: ComponentFixture<AddFunds>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddFunds]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddFunds);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
