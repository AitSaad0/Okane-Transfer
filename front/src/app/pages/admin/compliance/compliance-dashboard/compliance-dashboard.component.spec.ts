import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceDashboardComponent } from './compliance-dashboard.component';

describe('ComplianceDashboardComponent', () => {
  let component: ComplianceDashboardComponent;
  let fixture: ComponentFixture<ComplianceDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ComplianceDashboardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceDashboardComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
