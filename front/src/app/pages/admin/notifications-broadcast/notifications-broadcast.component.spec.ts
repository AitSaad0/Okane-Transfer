import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationsBroadcastComponent } from './notifications-broadcast.component';

describe('NotificationsBroadcastComponent', () => {
  let component: NotificationsBroadcastComponent;
  let fixture: ComponentFixture<NotificationsBroadcastComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotificationsBroadcastComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsBroadcastComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
