import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { AgenceService } from '../../agencies/services/agence.service';
import { UserRole } from '../models/user.model';
import { AgenceResponseDto } from '../../agencies/models/agence.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css'],
})
export class UserFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  userId: number | null = null;
  loading = false;
  saving = false;
  error = '';
  agences: AgenceResponseDto[] = [];

  // CLIENT retiré — les clients s'inscrivent eux-mêmes via /register
  readonly roles: { value: UserRole; label: string }[] = [
    { value: 'ADMIN',   label: 'Administrateur' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'AGENT',   label: 'Agent' },
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private agenceService: AgenceService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadAgences();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.userId = +id;
      this.loadUser(+id);
      // En mode édition, le mot de passe n'est pas requis
      this.form.get('password')?.clearValidators();
      this.form.get('password')?.updateValueAndValidity();
    }

    // Surveiller le rôle pour afficher/masquer le select agence
    this.form.get('role')?.valueChanges.subscribe((role) => {
      const agenceCtrl = this.form.get('agenceId');
      if (role === 'AGENT' || role === 'MANAGER') {
        agenceCtrl?.setValidators(Validators.required);
      } else {
        agenceCtrl?.clearValidators();
        agenceCtrl?.setValue(null);
      }
      agenceCtrl?.updateValueAndValidity();
    });
  }

  private buildForm(): void {
    this.form = this.fb.group({
      prenom:    ['', Validators.required],
      nom:       ['', Validators.required],
      email:     ['', [Validators.required, Validators.email]],
      telephone: [''],
      role:      ['MANAGER', Validators.required],   // défaut MANAGER, plus ROLE_CLIENT
      password:  ['', [Validators.required, Validators.minLength(8)]],
      agenceId:  [null],
    });
  }

  private loadUser(id: number): void {
    this.loading = true;
    this.userService.getUserById(id).subscribe({
      next: (user) => {
        this.form.patchValue({
          prenom:    user.prenom,
          nom:       user.nom,
          email:     user.email,
          telephone: user.telephone ?? '',
          role:      user.role,
          agenceId:  user.agenceId,
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = "Impossible de charger l'utilisateur.";
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadAgences(): void {
    this.agenceService.getAllAgencesActives().subscribe({
      next: (res) => {
        this.agences = res.content;
      },
    });
  }

  get showAgence(): boolean {
    const r = this.form.get('role')?.value;
    return r === 'AGENT' || r === 'MANAGER';   // sans préfixe ROLE_
  }

  get title(): string {
    return this.isEdit ? "Modifier l'utilisateur" : 'Créer un utilisateur';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.error = '';

    if (this.isEdit && this.userId) {
      const dto = {
        nom:       this.form.value.nom,
        prenom:    this.form.value.prenom,
        telephone: this.form.value.telephone || undefined,
        agenceId:  this.form.value.agenceId  || undefined,
      };
      this.userService.updateUser(this.userId, dto).subscribe({
        next: () => this.router.navigate(['/admin/users']),
        error: () => {
          this.error = 'Erreur lors de la mise à jour.';
          this.saving = false;
        },
      });
    } else {
      const dto = {
        email:     this.form.value.email,
        password:  this.form.value.password,
        nom:       this.form.value.nom,
        prenom:    this.form.value.prenom,
        telephone: this.form.value.telephone || undefined,
        role:      this.form.value.role,
        agenceId:  this.form.value.agenceId  || undefined,
      };
      this.userService.createUser(dto).subscribe({
        next: () => this.router.navigate(['/admin/users']),
        error: () => {
          this.error = 'Erreur lors de la création.';
          this.saving = false;
        },
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/users']);
  }

  hasError(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}
