import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { Role, UserResponseDTO, UserRole } from '../models/user.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';


@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent, ConfirmDialogComponent,TranslatePipe],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
})
export class UserListComponent implements OnInit {
  users: UserResponseDTO[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;
  loading = false;
  error = '';

  // Filtres
  searchText = '';
  selectedRole: UserRole | '' = '';
  selectedStatus: '' | 'true' | 'false' = '';

  // Dialog suppression
  showDeleteDialog = false;
  userToDelete: UserResponseDTO | null = null;

  // Dialog status
  showStatusDialog = false;
  userToToggle: UserResponseDTO | null = null;

  // ⚠️ readonly supprimé pour permettre la réassignation dans loadFilterLabels()
  roles: { value: UserRole | ''; label: string }[] = [];
  statuses: { value: '' | 'true' | 'false'; label: string }[] = [];

  constructor(
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.loadFilterLabels();
    this.translate.onLangChange.subscribe(() => {
      this.loadFilterLabels();
      this.cdr.detectChanges();
    });
    this.loadUsers();
  }

  private loadFilterLabels(): void {
    this.roles = [
      { value: '', label: this.translate.instant('USERS.ALL_ROLES') },
      { value: 'ADMIN', label: this.translate.instant('USERS.ROLES.ROLE_ADMIN') },
      { value: 'MANAGER', label: this.translate.instant('USERS.ROLES.ROLE_MANAGER') },
      { value: 'AGENT', label: this.translate.instant('USERS.ROLES.ROLE_AGENT') },
      { value: 'CLIENT', label: this.translate.instant('USERS.ROLES.ROLE_CLIENT') },
    ];
    this.statuses = [
      { value: '', label: this.translate.instant('USERS.ALL_STATUSES') },
      { value: 'true', label: this.translate.instant('USERS.STATUS.ACTIVE') },
      { value: 'false', label: this.translate.instant('USERS.STATUS.SUSPENDED') },
    ];
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    const filters: { role?: Role; active?: boolean } = {};
    if (this.selectedRole) filters.role = this.selectedRole as Role;
    if (this.selectedStatus !== '') filters.active = this.selectedStatus === 'true';

    this.userService.getAllUsers(this.currentPage, this.pageSize, 'id', filters).subscribe({
      next: (res) => {
        this.users = [...res.content];
        this.totalElements = res.totalElements;
        this.totalPages = res.totalPages;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.error = 'Impossible de charger les utilisateurs.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredUsers(): UserResponseDTO[] {
    if (!this.searchText.trim()) return this.users;
    const q = this.searchText.toLowerCase();
    return this.users.filter(
      (u) =>
        u.nom.toLowerCase().includes(q) ||
        u.prenom.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q),
    );
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.currentPage = p;
    this.loadUsers();
  }

  goToCreate(): void {
    this.router.navigate(['/admin/users/create']);
  }
  goToEdit(id: number): void {
    this.router.navigate(['/admin/users', id, 'edit']);
  }
  goToDetail(id: number): void {
    this.router.navigate(['/admin/users', id, 'detail']);
  }

  // ── Toggle status ─────────────────────────────────────────────────────────
  openStatusDialog(user: UserResponseDTO): void {
    this.userToToggle = user;
    this.showStatusDialog = true;
  }

  confirmToggleStatus(): void {
    if (!this.userToToggle) return;
    this.userService
      .updateStatus(this.userToToggle.id, { active: !this.userToToggle.active })
      .subscribe({
        next: () => {
          this.showStatusDialog = false;
          this.userToToggle = null;
          this.loadUsers();
        },
        error: () => {
          this.error = 'Erreur lors de la mise à jour du statut.';
          this.showStatusDialog = false;
        },
      });
  }

  // ── Suppression RGPD ──────────────────────────────────────────────────────
  openDeleteDialog(user: UserResponseDTO): void {
    this.userToDelete = user;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (!this.userToDelete) return;
    this.userService.deleteUser(this.userToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog = false;
        this.userToDelete = null;
        this.loadUsers();
      },
      error: () => {
        this.error = 'Erreur lors de la suppression.';
        this.showDeleteDialog = false;
      },
    });
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
