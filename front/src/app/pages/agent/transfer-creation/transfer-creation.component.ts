import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { TransferService } from './services/transfer.service';
import { Corridor, PaysItem, SimulationResponse, TransfertResponse } from './models/transfer.model';

@Component({
  selector: 'app-transfer-creation',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './transfer-creation.component.html',
  styleUrl: './transfer-creation.component.css'
})
export class TransferCreationComponent implements OnInit {
  currentStep = 1;
  loading = false;
  success = false;
  error = '';
  result: TransfertResponse | null = null;

  corridors: Corridor[] = [];
  selectedCorridor: Corridor | null = null;
  countries: PaysItem[] = [];
  simulation: SimulationResponse | null = null;
  simulationLoading = false;

  expediteurForm: FormGroup;
  beneficiaireForm: FormGroup;
  montantForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private transferService: TransferService,
    private cdr: ChangeDetectorRef
  ) {
    this.expediteurForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      numPieceIdentite: [''],
      telephone: ['', Validators.required],
      paysId: [null, Validators.required],
      email: ['']
    });

    this.beneficiaireForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      telephone: ['', Validators.required],
      paysId: [null, Validators.required]
    });

    this.montantForm = this.fb.group({
      montantEnvoye: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.transferService.getAllCountries().subscribe({
      next: (data) => this.countries = data,
      error: () => {}
    });

    this.transferService.getActiveCorridors().subscribe({
      next: (data) => {
        this.corridors = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'TRANSFER.CORRIDOR_LOAD_ERROR';
        this.cdr.detectChanges();
      }
    });
  }

  getCountryName(id: number | null): string {
    const country = this.countries.find(c => c.id === id);
    return country ? country.nom : '';
  }

  selectCorridor(corridor: Corridor): void {
    this.selectedCorridor = corridor;
    this.expediteurForm.patchValue({ paysId: corridor.paysOrigineId });
    this.beneficiaireForm.patchValue({ paysId: corridor.paysDestinationId });
    this.currentStep = 2;
  }

  nextStep(): void {
    if (this.currentStep === 2 && this.expediteurForm.valid) {
      this.currentStep = 3;
    } else if (this.currentStep === 3 && this.beneficiaireForm.valid) {
      this.currentStep = 4;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) this.currentStep--;
  }

  calculerFrais(): void {
    if (this.montantForm.invalid || !this.selectedCorridor) return;
    this.simulationLoading = true;
    this.error = '';

    this.transferService.simulateFees({
      corridorId: this.selectedCorridor.id,
      montant: this.montantForm.value.montantEnvoye
    }).subscribe({
      next: (res) => {
        this.simulation = res;
        this.simulationLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'TRANSFER.SIMULATION_ERROR';
        this.simulationLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  submit(): void {
    if (!this.simulation || !this.selectedCorridor) return;
    this.loading = true;
    this.error = '';

    const request = {
      expediteur: this.expediteurForm.value,
      beneficiaire: this.beneficiaireForm.value,
      montantEnvoye: this.simulation.montantEnvoye,
      corridorId: this.selectedCorridor.id
    };

    this.transferService.creerTransfert(request).subscribe({
      next: (res) => {
        this.result = res;
        this.success = true;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err.error?.message ?? 'TRANSFER.ERROR';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  downloadPdf(): void {
    if (!this.result?.id) return;
    const token = localStorage.getItem('accessToken') || '';
    fetch('/api/v1/agent/transfers/' + this.result.id + '/recu', {
      headers: { 'Authorization': 'Bearer ' + token }
    }).then(res => res.blob()).then(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'recu-transfert-' + this.result!.codeRetrait + '.pdf';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  resetForm(): void {
    this.currentStep = 1;
    this.success = false;
    this.result = null;
    this.error = '';
    this.selectedCorridor = null;
    this.simulation = null;
    this.expediteurForm.reset();
    this.beneficiaireForm.reset();
    this.montantForm.reset();
    this.cdr.detectChanges();
  }
}
