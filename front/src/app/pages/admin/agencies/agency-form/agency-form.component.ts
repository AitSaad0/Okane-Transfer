import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AgenceService } from '../services/agence.service';
import { PaysResponseDTO } from '../models/agence.model';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-agency-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,TranslatePipe],
  templateUrl: './agency-form.component.html',
  styleUrls: ['./agency-form.component.css'],
})
export class AgencyFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  agencyId: number | null = null;
  loading = false;
  saving = false;
  error = '';
  pays: PaysResponseDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private agenceService: AgenceService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.buildForm();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit   = true;
      this.agencyId = +id;
    }

    // Charge les pays UNE seule fois, puis l'agence si en mode édition
    this.loadPays();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nom:               ['', Validators.required],
      adresse:           ['', Validators.required],
      ville:             ['', Validators.required],
      codePostal:        ['', Validators.required],
      plafondJournalier: [null, [Validators.required, Validators.min(1)]],
      paysId:            [null, Validators.required],
    });
  }

  private loadAgency(id: number): void {
    this.loading = true;
    this.agenceService.getAgenceById(id).subscribe({
      next: agency => {
        // paysId n'est pas renvoyé directement par AgenceResponseDto (seulement paysNom/paysCode)
        // On essaie de retrouver le pays correspondant via codeIso
        const matchedPays = this.pays.find(p => p.codeIso === agency.paysCode);
        this.form.patchValue({
          nom:               agency.nom,
          adresse:           agency.adresse,
          ville:             agency.ville,
          codePostal:        agency.codePostal,
          plafondJournalier: agency.plafondJournalier,
          paysId:            matchedPays ? matchedPays.id : null,
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Impossible de charger l\'agence.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadPays(): void {
    this.agenceService.getAllPays().subscribe({
      next: p => {
        this.pays = p;
        this.cdr.detectChanges();
        // Une fois les pays chargés, charge l'agence (mode édition)
        if (this.isEdit && this.agencyId) {
          this.loadAgency(this.agencyId);
        }
      },
      error: () => {
        // Le select pays restera vide, mais on charge l'agence quand même
        if (this.isEdit && this.agencyId) {
          this.loadAgency(this.agencyId);
        }
      },
    });
  }

  get title(): string {
    return this.isEdit ? 'Modifier l\'agence' : 'Créer une agence';
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error  = '';

    const dto = {
      nom:               this.form.value.nom,
      adresse:           this.form.value.adresse,
      ville:             this.form.value.ville,
      codePostal:        this.form.value.codePostal,
      plafondJournalier: this.form.value.plafondJournalier,
      paysId:            this.form.value.paysId,
    };

    if (this.isEdit && this.agencyId) {
      this.agenceService.updateAgence(this.agencyId, dto).subscribe({
        next: () => this.router.navigate(['/admin/agencies']),
        error: () => { this.error = 'Erreur lors de la mise à jour.'; this.saving = false; },
      });
    } else {
      this.agenceService.createAgence(dto).subscribe({
        next: () => this.router.navigate(['/admin/agencies']),
        error: () => { this.error = 'Erreur lors de la création.'; this.saving = false; },
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/agencies']);
  }

  hasError(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
