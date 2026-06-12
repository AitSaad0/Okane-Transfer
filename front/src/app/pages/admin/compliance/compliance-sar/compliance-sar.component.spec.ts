import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplianceSarComponent } from './compliance-sar.component';

describe('ComplianceSarComponent', () => {
  let component: ComplianceSarComponent;
  let fixture: ComponentFixture<ComplianceSarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ComplianceSarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplianceSarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
