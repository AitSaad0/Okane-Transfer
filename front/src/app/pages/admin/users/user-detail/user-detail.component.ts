import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { UserResponseDTO, ClientActivityResponseDto } from '../models/user.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslatePipe } from '@ngx-translate/core';

type Tab = 'profil' | 'activite';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule, StatusBadgeComponent, ConfirmDialogComponent, TranslatePipe],
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.css'],
})
export class UserDetailComponent implements OnInit {
  user: UserResponseDTO | null = null;
  activities: ClientActivityResponseDto[] = [];
  activeTab: Tab = 'profil';
  loading = false;
  loadingActivity = false;
  error = '';

  showDeleteDialog = false;
  showStatusDialog = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadUser(+id);
  }

  loadUser(id: number): void {
    this.loading = true;
    this.userService.getUserById(id).subscribe({
      next: (u) => {
        this.user = u;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = "Impossible de charger l'utilisateur.";
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  setTab(tab: Tab): void {
    this.activeTab = tab;
    if (tab === 'activite' && this.user && this.activities.length === 0) {
      this.loadActivity(this.user.id);
    }
  }

  loadActivity(id: number): void {
    this.loadingActivity = true;
    this.userService.getActivity(id).subscribe({
      next: (a) => {
        this.activities = a;
        this.loadingActivity = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingActivity = false;
        this.cdr.detectChanges();
      },
    });
  }

  goToEdit(): void {
    if (this.user) this.router.navigate(['/admin/users', this.user.id, 'edit']);
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }

  // ── Toggle statut ─────────────────────────────────────────────────────────
  confirmToggleStatus(): void {
    if (!this.user) return;
    this.userService.updateStatus(this.user.id, { active: !this.user.active }).subscribe({
      next: (updated) => {
        this.user = updated;
        this.showStatusDialog = false;
      },
      error: () => {
        this.error = 'Erreur mise à jour statut.';
        this.showStatusDialog = false;
      },
    });
  }

  // ── Suppression RGPD ──────────────────────────────────────────────────────
  confirmDelete(): void {
    if (!this.user) return;
    this.userService.deleteUser(this.user.id).subscribe({
      next: () => this.router.navigate(['/admin/users']),
      error: () => {
        this.error = 'Erreur lors de la suppression.';
        this.showDeleteDialog = false;
      },
    });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
