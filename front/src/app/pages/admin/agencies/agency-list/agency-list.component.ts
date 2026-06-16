import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AgenceService } from '../services/agence.service';
import { AgenceResponseDto, StatutAgence, PaysResponseDTO } from '../models/agence.model';
import { PageResponseDto } from '../models/page-response.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-agency-list',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent, ConfirmDialogComponent,TranslatePipe],
  templateUrl: './agency-list.component.html',
  styleUrls: ['./agency-list.component.css'],
})
export class AgencyListComponent implements OnInit {
  agencies: AgenceResponseDto[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;
  loading = false;
  error = '';

  searchText = '';
  selectedStatus: StatutAgence | '' = '';
  selectedPaysId: number | '' = '';
  pays: PaysResponseDTO[] = [];

  showStatusDialog = false;
  agencyToToggle: AgenceResponseDto | null = null;

  get statuses(): { value: StatutAgence | ''; label: string }[] {
    return [
      { value: '',          label: this.translate.instant('AGENCIES.ALL_STATUSES') },
      { value: 'ACTIVE',    label: this.translate.instant('AGENCIES.STATUS.ACTIVE') },
      { value: 'SUSPENDUE', label: this.translate.instant('AGENCIES.STATUS.SUSPENDED') },
    ];
  }

  constructor(
    private agenceService: AgenceService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.loadAgencies();
    this.loadPays();
  }

  loadPays(): void {
    this.agenceService.getAllPays().subscribe({
      next: p => { this.pays = p; this.cdr.detectChanges(); },
      error: () => { /* select pays restera vide */ },
    });
  }

  loadAgencies(): void {
    this.loading = true;
    this.error = '';
    const filters: { statut?: StatutAgence; paysId?: number } = {};
    if (this.selectedStatus) filters.statut = this.selectedStatus;
    if (this.selectedPaysId) filters.paysId = +this.selectedPaysId;

    this.agenceService.getAllAgences(this.currentPage, this.pageSize, 'id', filters).subscribe({
      next: (res: PageResponseDto<AgenceResponseDto>) => {
        this.agencies      = [...res.content];
        this.totalElements = res.totalElements;
        this.totalPages    = res.totalPages;
        this.loading       = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Impossible de charger les agences.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredAgencies(): AgenceResponseDto[] {
    if (!this.searchText.trim()) return this.agencies;
    const q = this.searchText.toLowerCase();
    return this.agencies.filter(a =>
      a.nom.toLowerCase().includes(q) ||
      a.ville.toLowerCase().includes(q) ||
      a.paysNom.toLowerCase().includes(q)
    );
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadAgencies();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.currentPage = p;
    this.loadAgencies();
  }

  goToCreate(): void {
    this.router.navigate(['/admin/agencies/create']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/admin/agencies', id, 'edit']);
  }

  goToAgents(id: number): void {
    this.router.navigate(['/admin/agencies', id, 'agents']);
  }

  goToDashboard(id: number): void {
    this.router.navigate(['/admin/agencies', id, 'dashboard']);
  }

  // ── Toggle status ────────────────────────────────────────────────────────
  openStatusDialog(agency: AgenceResponseDto): void {
    this.agencyToToggle   = agency;
    this.showStatusDialog = true;
  }

  confirmToggleStatus(): void {
    if (!this.agencyToToggle) return;
    const newStatut: StatutAgence = this.agencyToToggle.statut === 'ACTIVE' ? 'SUSPENDUE' : 'ACTIVE';
    this.agenceService.updateStatus(this.agencyToToggle.id, { statut: newStatut }).subscribe({
      next: () => { this.showStatusDialog = false; this.agencyToToggle = null; this.loadAgencies(); },
      error: () => { this.error = 'Erreur lors de la mise à jour du statut.'; this.showStatusDialog = false; },
    });
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
