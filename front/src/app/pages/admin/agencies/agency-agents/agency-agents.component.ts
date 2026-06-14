import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AgentService } from '../services/agent.service';
import { AgenceService } from '../services/agence.service';
import { AgentDetailResponseDto } from '../models/agence.model';
import { AgenceResponseDto } from '../models/agence.model';
import { UserService } from '../../users/services/user.service';
import { UserResponseDTO } from '../../users/models/user.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-agency-agents',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent, ConfirmDialogComponent,TranslatePipe],
  templateUrl: './agency-agents.component.html',
  styleUrls: ['./agency-agents.component.css'],
})
export class AgencyAgentsComponent implements OnInit {
  agencyId!: number;
  agency: AgenceResponseDto | null = null;
  agents: AgentDetailResponseDto[] = [];
  availableAgents: UserResponseDTO[] = [];
  selectedUserId: number | null = null;

  loading = false;
  loadingAvailable = false;
  assigning = false;
  error = '';

  // Dialogs
  showRemoveDialog = false;
  showStatusDialog = false;
  showAssignDialog = false;
  targetAgent: AgentDetailResponseDto | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private agentService: AgentService,
    private agenceService: AgenceService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.agencyId = +id;
    this.loadAgency();
    this.loadAgents();
  }

  loadAgency(): void {
    this.agenceService.getAgenceById(this.agencyId).subscribe({
      next: (a) => {
        this.agency = a;
        this.cdr.detectChanges();
      },
    });
  }

  loadAgents(): void {
    this.loading = true;
    this.agentService.getAgentsByAgence(this.agencyId).subscribe({
      next: (agents) => {
        this.agents = [...agents];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Impossible de charger les agents.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // ── Affecter un agent ───────────────────────────────────────────────────
  openAssignDialog(): void {
    this.showAssignDialog = true;
    this.selectedUserId = null;
    this.loadAvailableAgents();
  }

  loadAvailableAgents(): void {
    this.loadingAvailable = true;
    this.userService.getAllUsers(0, 1000, 'id', { role: 'AGENT', active: true }).subscribe({
      next: (res) => {
        // Ne garder que les agents non affectés à une agence
        this.availableAgents = res.content.filter((u) => !u.agenceId);
        this.loadingAvailable = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingAvailable = false;
        this.cdr.detectChanges();
      },
    });
  }

  confirmAssign(): void {
    if (!this.selectedUserId) return;
    this.assigning = true;
    this.agentService.assignAgent(this.agencyId, { userId: this.selectedUserId }).subscribe({
      next: () => {
        this.assigning = false;
        this.showAssignDialog = false;
        this.loadAgents();
      },
      error: () => {
        this.error = "Erreur lors de l'affectation de l'agent.";
        this.assigning = false;
        this.cdr.detectChanges();
      },
    });
  }

  // ── Suspendre / réactiver ───────────────────────────────────────────────
  openStatusDialog(agent: AgentDetailResponseDto): void {
    this.targetAgent = agent;
    this.showStatusDialog = true;
  }

  confirmToggleStatus(): void {
    if (!this.targetAgent) return;
    this.agentService
      .updateAgentStatus(this.agencyId, this.targetAgent.id, { active: !this.targetAgent.active })
      .subscribe({
        next: () => {
          this.showStatusDialog = false;
          this.targetAgent = null;
          this.loadAgents();
        },
        error: () => {
          this.error = 'Erreur lors de la mise à jour du statut.';
          this.showStatusDialog = false;
        },
      });
  }

  // ── Retirer un agent ─────────────────────────────────────────────────────
  openRemoveDialog(agent: AgentDetailResponseDto): void {
    this.targetAgent = agent;
    this.showRemoveDialog = true;
  }

  confirmRemove(): void {
    if (!this.targetAgent) return;
    this.agentService.removeAgent(this.agencyId, this.targetAgent.id).subscribe({
      next: () => {
        this.showRemoveDialog = false;
        this.targetAgent = null;
        this.loadAgents();
      },
      error: () => {
        this.error = "Erreur lors du retrait de l'agent.";
        this.showRemoveDialog = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/agencies']);
  }

  formatDate(iso: string | null): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
