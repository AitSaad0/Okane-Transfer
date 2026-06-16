import {
  Component,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ManagerService } from './services/manager.service';
import { ManagerDashboardResponseDTO } from './models/manager-dashboard.model';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manager-dashboard.component.html',
  styleUrls: ['./manager-dashboard.component.css']
})
export class ManagerDashboardComponent implements OnInit {
  dashboard: ManagerDashboardResponseDTO | null = null;
  loading = true;
  error: string | null = null;
  noAgence = false;

  constructor(
    private managerService: ManagerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;
    this.noAgence = false;
    this.cdr.markForCheck();

    this.managerService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 400) {
          this.noAgence = true;
        } else {
          this.error = 'Erreur lors du chargement du dashboard';
        }
        this.cdr.markForCheck();
      }
    });
  }

  refresh(): void {
    this.loadDashboard();
  }
}
