import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagerTransfersComponent } from './manager-transfers.component';

describe('ManagerTransfersComponent', () => {
  let component: ManagerTransfersComponent;
  let fixture: ComponentFixture<ManagerTransfersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManagerTransfersComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ManagerTransfersComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
