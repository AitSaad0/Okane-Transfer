import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TwoFactorService } from '../../core/services/two-factor.service';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-two-fa-verify',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, RouterLink],
  templateUrl: './two-fa-verify.component.html',
  styleUrl: './two-fa-verify.component.css'
})
export class TwoFaVerifyComponent implements OnInit {
  form: FormGroup;
  loading = false;
  resending = false;
  error = '';
  email = '';

  constructor(
    private fb: FormBuilder,
    private twoFactorService: TwoFactorService,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });
  }

  ngOnInit(): void {
    const payload = this.tokenService.decodeToken(
      this.tokenService.getAccessToken() ?? ''
    );
    this.email = payload?.sub ?? '';
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    this.twoFactorService.verifyOtp(this.email, this.form.value.code).subscribe({
      next: () => this.router.navigate(['/client/dashboard']),
      error: err => {
        this.error = err.error?.message ?? '2FA.ERROR';
        this.loading = false;
      }
    });
  }

  resend(): void {
    this.resending = true;
    this.twoFactorService.sendOtp(this.email).subscribe({
      next: () => this.resending = false,
      error: () => this.resending = false
    });
  }
}
