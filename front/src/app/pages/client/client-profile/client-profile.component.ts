import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ClientService, ActivityItem } from '../services/client.service';

@Component({
  selector: 'app-client-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslateModule],
  templateUrl: './client-profile.component.html',
  styleUrls: ['./client-profile.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientProfileComponent implements OnInit {
  form!: FormGroup;
  loading = true;
  saving = false;
  success = false;
  error = '';
  activeTab: 'profile' | 'activity' = 'profile';
  activity: ActivityItem[] = [];
  loadingActivity = false;

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      prenom: ['', Validators.required],
      nom: ['', Validators.required],
      telephone: ['', Validators.required],
      emailNotif: [true],
      pushNotif: [false],
      smsNotif: [false],
    });
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.clientService.getProfile().subscribe({
      next: (data) => {
        this.form.patchValue({
          prenom: data.prenom,
          nom: data.nom,
          telephone: data.telephone,
          emailNotif: data.notificationEmail ?? true,
          pushNotif: data.notificationPush ?? false,
          smsNotif: data.notificationSms ?? false,
        });
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Erreur chargement profil';
        this.loading = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadActivity(): void {
    if (this.activity.length > 0) return;
    this.loadingActivity = true;
    this.clientService.getActivity().subscribe({
      next: (data) => {
        this.activity = data;
        this.loadingActivity = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingActivity = false;
        this.cdr.markForCheck();
      },
    });
  }

  setTab(tab: 'profile' | 'activity'): void {
    this.activeTab = tab;
    if (tab === 'activity') this.loadActivity();
  }

  onSubmit(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;
    this.success = false;
    this.error = '';

    const payload = {
      prenom: this.form.value.prenom,
      nom: this.form.value.nom,
      telephone: this.form.value.telephone,
      notificationEmail: this.form.value.emailNotif,
      notificationPush: this.form.value.pushNotif,
      notificationSms: this.form.value.smsNotif,
    };

    this.clientService.updateProfile(payload).subscribe({
      next: () => {
        this.saving = false;
        this.success = true;
        this.cdr.markForCheck();
      },
      error: () => {
        this.saving = false;
        this.error = 'Erreur lors de la mise à jour';
        this.cdr.markForCheck();
      },
    });
  }

  hasError(field: string): boolean {
    const c = this.form.get(field);
    return !!(c && c.invalid && c.touched);
  }
}
