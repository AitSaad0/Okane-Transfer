import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-profile-security',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule],
  templateUrl: './profile-security.component.html',
  styleUrl: './profile-security.component.css'
})
export class ProfileSecurityComponent {
  form: FormGroup;
  loading = false;
  success = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient
  ) {
    this.form = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword:     ['', [Validators.required, Validators.minLength(8)]],
      confirm:         ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    if (this.form.value.newPassword !== this.form.value.confirm) {
      this.error = 'PROFILE_SECURITY.MISMATCH';
      return;
    }
    this.loading = true;
    this.error = '';

    this.http.put('/api/auth/change-password', {
      currentPassword: this.form.value.currentPassword,
      newPassword:     this.form.value.newPassword
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        this.form.reset();
      },
      error: err => {
        this.error = err.error?.message ?? 'PROFILE_SECURITY.ERROR';
        this.loading = false;
      }
    });
  }
}
