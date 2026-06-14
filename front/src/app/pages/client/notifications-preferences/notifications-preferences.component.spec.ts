import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationsPreferencesComponent } from './notifications-preferences.component';

describe('NotificationsPreferencesComponent', () => {
  let component: NotificationsPreferencesComponent;
  let fixture: ComponentFixture<NotificationsPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotificationsPreferencesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsPreferencesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
