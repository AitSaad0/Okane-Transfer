import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminWatchlistComponent } from './admin-watchlist.component';

describe('AdminWatchlistComponent', () => {
  let component: AdminWatchlistComponent;
  let fixture: ComponentFixture<AdminWatchlistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminWatchlistComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminWatchlistComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
