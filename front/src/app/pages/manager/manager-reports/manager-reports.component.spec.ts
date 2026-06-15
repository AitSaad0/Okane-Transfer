import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagerReportsComponent } from './manager-reports.component';

describe('ManagerReportsComponent', () => {
  let component: ManagerReportsComponent;
  let fixture: ComponentFixture<ManagerReportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManagerReportsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerReportsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
