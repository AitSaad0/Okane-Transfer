// manager-dashboard.component.ts
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
    this.cdr.markForCheck();

    this.managerService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du dashboard';
        this.loading = false;
        this.cdr.markForCheck();
        console.error(err);
      }
    });
  }

  refresh(): void {
    this.loadDashboard();
  }
}
