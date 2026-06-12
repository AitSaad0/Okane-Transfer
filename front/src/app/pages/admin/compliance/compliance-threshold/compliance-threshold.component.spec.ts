import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceThresholdComponent } from './compliance-threshold.component';

describe('ComplianceThresholdComponent', () => {
  let component: ComplianceThresholdComponent;
  let fixture: ComponentFixture<ComplianceThresholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ComplianceThresholdComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceThresholdComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
