import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AgenceService } from '../services/agence.service';
import { AgenceDashboardResponseDto } from '../models/agence.model';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-agency-dashboard',
  standalone: true,
  imports: [CommonModule,TranslatePipe],
  templateUrl: './agency-dashboard.component.html',
  styleUrls: ['./agency-dashboard.component.css'],
})
export class AgencyDashboardComponent implements OnInit {
  agencyId!: number;
  dashboard: AgenceDashboardResponseDto | null = null;
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private agenceService: AgenceService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.agencyId = +id;
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = '';
    this.agenceService.getDashboard(this.agencyId).subscribe({
      next: (d) => {
        this.dashboard = d;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Impossible de charger le tableau de bord.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/agencies']);
  }

  // Pourcentage d'utilisation du plafond, borné à 100 pour la barre de progression
  get plafondPercentClamped(): number {
    if (!this.dashboard) return 0;
    return Math.min(this.dashboard.tauxUtilisationPlafond, 100);
  }

  get plafondBarColor(): string {
    const pct = this.dashboard?.tauxUtilisationPlafond ?? 0;
    if (pct >= 90) return 'bg-rose-500';
    if (pct >= 70) return 'bg-amber-500';
    return 'bg-emerald-500';
  }
}
