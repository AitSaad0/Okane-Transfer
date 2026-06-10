import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { PaymentService } from './services/payment.service';
import { TokenService } from '../../../core/services/token.service';
import { PaiementResponse } from './models/payment.model';

@Component({
  selector: 'app-transfer-payment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './transfer-payment.component.html',
  styleUrl: './transfer-payment.component.css'
})
export class TransferPaymentComponent {
  searchType: 'code' | 'telephone' = 'code';
  loading = false;
  error = '';
  transfert: PaiementResponse | null = null;
  transferts: PaiementResponse[] = [];
  paySuccess = false;
  paying = false;

  searchForm: FormGroup;
  validationForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private tokenService: TokenService,
    private cdr: ChangeDetectorRef
  ) {
    this.searchForm = this.fb.group({
      codeRetrait: ['', Validators.required],
      telephone: ['']
    });

    this.validationForm = this.fb.group({
      pieceIdentiteBeneficiaire: ['', Validators.required]
    });
  }

  toggleSearchType(): void {
    this.searchType = this.searchType === 'code' ? 'telephone' : 'code';
    this.error = '';
    this.transfert = null;
    this.transferts = [];
    this.paySuccess = false;
    this.searchForm.reset();
    this.validationForm.reset();
    this.cdr.detectChanges();
  }

  search(): void {
    this.error = '';
    this.transfert = null;
    this.transferts = [];
    this.paySuccess = false;
    this.paying = false;
    this.cdr.detectChanges();

    if (this.searchType === 'code') {
      const code = this.searchForm.value.codeRetrait;
      if (!code) {
        this.error = 'PAYMENT.CODE_REQUIRED';
        this.cdr.detectChanges();
        return;
      }
      this.loading = true;
      this.cdr.detectChanges();
      this.paymentService.searchByCode(code).subscribe({
        next: (res) => {
          this.transfert = res;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.error = err.error?.message ?? 'PAYMENT.NOT_FOUND';
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      const tel = this.searchForm.value.telephone;
      if (!tel) {
        this.error = 'PAYMENT.PHONE_REQUIRED';
        this.cdr.detectChanges();
        return;
      }
      this.loading = true;
      this.cdr.detectChanges();
      this.paymentService.searchByTelephone(tel).subscribe({
        next: (res) => {
          this.loading = false;
          if (res.length === 1) {
            this.transfert = res[0];
          } else {
            this.transferts = res;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.error = err.error?.message ?? 'PAYMENT.NOT_FOUND';
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  viewDetail(t: PaiementResponse): void {
    this.transfert = t;
    this.transferts = [];
    this.cdr.detectChanges();
  }

  confirmPayment(): void {
    if (!this.transfert || this.validationForm.invalid) return;
    this.paying = true;
    this.error = '';
    this.cdr.detectChanges();

    const request = {
      transfertId: this.transfert.id,
      pieceIdentiteBeneficiaire: this.validationForm.value.pieceIdentiteBeneficiaire,
      codeRetrait: this.transfert.codeRetrait
    };

    this.paymentService.confirmPayment(this.transfert.id, request).subscribe({
      next: (res) => {
        this.transfert = res;
        this.paySuccess = true;
        this.paying = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err.error?.message ?? 'PAYMENT.ERROR';
        this.paying = false;
        this.cdr.detectChanges();
      }
    });
  }

  downloadReceipt(): void {
    if (!this.transfert?.id) return;
    const token = this.tokenService.getAccessToken();
    if (!token) return;
    const url = '/api/v1/agent/transfers/' + this.transfert.id + '/recu-paiement';
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    xhr.responseType = 'blob';
    xhr.onload = () => {
      if (xhr.status === 200) {
        const blob = xhr.response;
        const dlUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = dlUrl;
        a.download = 'recu-paiement-' + this.transfert!.codeRetrait + '.pdf';
        a.click();
        setTimeout(() => URL.revokeObjectURL(dlUrl), 5000);
      } else {
        this.error = 'PAYMENT.DOWNLOAD_ERROR';
        this.cdr.detectChanges();
      }
    };
    xhr.onerror = () => {
      this.error = 'PAYMENT.DOWNLOAD_ERROR';
      this.cdr.detectChanges();
    };
    xhr.send();
  }

  resetAll(): void {
    this.transfert = null;
    this.transferts = [];
    this.paySuccess = false;
    this.error = '';
    this.paying = false;
    this.searchForm.reset();
    this.validationForm.reset();
    this.cdr.detectChanges();
  }
}
