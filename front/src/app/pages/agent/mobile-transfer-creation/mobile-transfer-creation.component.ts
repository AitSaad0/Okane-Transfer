import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MobileTransferService } from './services/mobile-transfer.service';
import { Corridor, PaysItem, SimulationResponse, MobileTransfertResponse, OperateurMobile, OperateurOption } from './models/mobile-transfer.model';

@Component({
  selector: 'app-mobile-transfer-creation',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './mobile-transfer-creation.component.html',
  styleUrl: './mobile-transfer-creation.component.css'
})
export class MobileTransferCreationComponent implements OnInit {
  currentStep = 1;
  loading = false;
  success = false;
  error = '';
  result: MobileTransfertResponse | null = null;

  corridors: Corridor[] = [];
  selectedCorridor: Corridor | null = null;
  countries: PaysItem[] = [];
  simulation: SimulationResponse | null = null;
  simulationLoading = false;
  operateurChoisi: OperateurMobile | null = null;

  operateurs: OperateurOption[] = [
    { code: OperateurMobile.ORANGE_MONEY, label: 'Orange Money', color: 'orange', initial: 'O' },
    { code: OperateurMobile.WAVE, label: 'Wave', color: 'teal', initial: 'W' },
    { code: OperateurMobile.MPESA, label: 'M-Pesa', color: 'red', initial: 'M' }
  ];

  get operateurColor(): string {
    return this.operateurs.find(o => o.code === this.operateurChoisi)?.color ?? '#3730A3';
  }

  operateurCardStyle(op: OperateurOption, selected: boolean): any {
    if (!selected) return {};
    return {
      '--brand': op.color,
      'border-color': op.color,
      'background-color': op.color + '0d',
    };
  }

  expediteurForm: FormGroup;
  beneficiaireForm: FormGroup;
  montantForm: FormGroup;

  totalSteps = 7;

  constructor(
    private fb: FormBuilder,
    private mobileTransferService: MobileTransferService,
    private cdr: ChangeDetectorRef
  ) {
    this.expediteurForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      numPieceIdentite: [''],
      telephone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s\-]{6,15}$/)]],
      paysId: [null, Validators.required],
      email: ['', [Validators.email]]
    });

    this.beneficiaireForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      telephone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s\-]{6,15}$/)]],
      paysId: [null, Validators.required]
    });

    this.montantForm = this.fb.group({
      montantEnvoye: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.mobileTransferService.getAllCountries().subscribe({
      next: (data) => this.countries = data,
      error: () => {}
    });

    this.mobileTransferService.getActiveCorridors().subscribe({
      next: (data) => {
        this.corridors = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'MOBILE_TRANSFER.CORRIDOR_LOAD_ERROR';
        this.cdr.detectChanges();
      }
    });
  }

  getCountryName(id: number | null): string {
    const country = this.countries.find(c => c.id === id);
    return country ? country.nom : '';
  }

  selectOperateur(op: OperateurMobile): void {
    this.operateurChoisi = op;
    this.currentStep = 2;
  }

  selectCorridor(corridor: Corridor): void {
    this.selectedCorridor = corridor;
    this.expediteurForm.patchValue({ paysId: corridor.paysOrigineId });
    this.beneficiaireForm.patchValue({ paysId: corridor.paysDestinationId });
    this.currentStep = 3;
  }

  nextStep(): void {
    if (this.currentStep === 3 && this.expediteurForm.valid) {
      this.currentStep = 4;
    } else if (this.currentStep === 4 && this.beneficiaireForm.valid) {
      this.currentStep = 5;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) this.currentStep--;
  }

  calculerFrais(): void {
    if (this.montantForm.invalid || !this.selectedCorridor) return;
    this.simulationLoading = true;
    this.error = '';
    this.cdr.detectChanges();

    this.mobileTransferService.simulateFees({
      corridorId: this.selectedCorridor.id,
      montant: this.montantForm.value.montantEnvoye
    }).subscribe({
      next: (res) => {
        this.simulation = res;
        this.simulationLoading = false;
        this.currentStep = 6;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'MOBILE_TRANSFER.SIMULATION_ERROR';
        this.simulationLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  submit(): void {
    if (!this.simulation || !this.selectedCorridor || !this.operateurChoisi) return;
    this.loading = true;
    this.error = '';
    this.cdr.detectChanges();

    const request = {
      operateur: this.operateurChoisi,
      expediteur: this.expediteurForm.value,
      beneficiaire: this.beneficiaireForm.value,
      montantEnvoye: this.simulation.montantEnvoye,
      corridorId: this.selectedCorridor.id
    };

    this.mobileTransferService.creerTransfertMobile(request).subscribe({
      next: (res) => {
        this.result = res;
        this.success = true;
        this.currentStep = 7;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err.error?.message ?? 'MOBILE_TRANSFER.ERROR';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  resetForm(): void {
    this.currentStep = 1;
    this.success = false;
    this.result = null;
    this.error = '';
    this.selectedCorridor = null;
    this.operateurChoisi = null;
    this.simulation = null;
    this.expediteurForm.reset();
    this.beneficiaireForm.reset();
    this.montantForm.reset();
    this.cdr.detectChanges();
  }
}
