import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { WatchlistEntryResponse, WatchlistEntryRequest, Client } from './models/watchlist.model';

type ModalMode = 'none' | 'add-existing' | 'add-new';

@Component({
  selector: 'app-admin-watchlist',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './admin-watchlist.component.html',
  styleUrls: ['./admin-watchlist.component.css']
})
export class AdminWatchlistComponent implements OnInit {

  entries: WatchlistEntryResponse[] = [];
  filteredEntries: WatchlistEntryResponse[] = [];
  loading = false;
  error: string | null = null;
  searchTerm = '';

  modalMode: ModalMode = 'none';
  submitting = false;
  submitError = '';
  submitSuccess = '';

  clientSearchTerm = '';
  clientResults: Client[] = [];
  selectedClient: Client | null = null;
  clientSearching = false;

  existingClientForm!: FormGroup;
  newEntryForm!: FormGroup;

  sourceOptions = ['OFAC', 'EU_SANCTIONS', 'UN_SANCTIONS', 'INTERPOL', 'LOCAL', 'OTHER'];

  private readonly kycBase = '/api/v1/admin/kyc';
  private readonly clientBase = '/api/v1/admin/clients';

  constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef   // ← add this
  ) {}

  ngOnInit(): void {
    this.buildForms();
    this.loadWatchlist();
  }

  private buildForms(): void {
    this.existingClientForm = this.fb.group({
      source: ['OFAC', Validators.required],
      reason: ['']
    });
    this.newEntryForm = this.fb.group({
      fullName: ['', Validators.required],
      idNumber: [''],
      source:   ['OFAC', Validators.required],
      reason:   ['']
    });
  }

  loadWatchlist(): void {
    this.loading = true;
    this.error = null;
    this.http.get<WatchlistEntryResponse[]>(`${this.kycBase}/watchlist`).subscribe({
      next: (data) => {
        this.entries = data;
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();   // ← add this
      },
      error: () => {
        this.error = 'Failed to load watchlist.';
        this.loading = false;
        this.cdr.detectChanges();   // ← add this
      }
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.toLowerCase().trim();
    this.filteredEntries = term
      ? this.entries.filter(e =>
        e.fullName.toLowerCase().includes(term) ||
        (e.idNumber ?? '').toLowerCase().includes(term) ||
        e.source.toLowerCase().includes(term)
      )
      : [...this.entries];
  }

  onSearch(value: string): void {
    this.searchTerm = value;
    this.applyFilter();
    this.cdr.detectChanges();   // ← add this
  }

  openModal(mode: ModalMode): void {
    this.modalMode = mode;
    this.submitError = '';
    this.submitSuccess = '';
    this.submitting = false;
    this.selectedClient = null;
    this.clientSearchTerm = '';
    this.clientResults = [];
    this.existingClientForm.reset({ source: 'OFAC', reason: '' });
    this.newEntryForm.reset({ source: 'OFAC' });
    this.cdr.detectChanges();   // ← add this
  }

  closeModal(): void {
    this.modalMode = 'none';
    this.cdr.detectChanges();   // ← add this
  }

  onClientSearch(query: string): void {
    this.clientSearchTerm = query;
    if (query.trim().length < 2) {
      this.clientResults = [];
      this.cdr.detectChanges();
      return;
    }
    this.clientSearching = true;
    const params = new HttpParams().set('search', query);
    this.http.get<Client[]>(this.clientBase, { params }).subscribe({
      next: (clients) => {
        this.clientResults = clients;
        this.clientSearching = false;
        this.cdr.detectChanges();   // ← add this
      },
      error: () => {
        this.clientSearching = false;
        this.cdr.detectChanges();   // ← add this
      }
    });
  }

  selectClient(client: Client): void {
    this.selectedClient = client;
    this.clientResults = [];
    this.clientSearchTerm = `${client.prenom} ${client.nom}`;
    this.cdr.detectChanges();   // ← add this
  }

  clearSelectedClient(): void {
    this.selectedClient = null;
    this.clientSearchTerm = '';
    this.cdr.detectChanges();   // ← add this
  }

  submitExistingClient(): void {
    if (!this.selectedClient || this.existingClientForm.invalid) return;
    const payload: WatchlistEntryRequest = {
      fullName: `${this.selectedClient.prenom} ${this.selectedClient.nom}`,
      idNumber: this.selectedClient.numPieceIdentite,
      source:   this.existingClientForm.value.source,
      reason:   this.existingClientForm.value.reason || undefined
    };
    this.submit(payload);
  }

  submitNewEntry(): void {
    if (this.newEntryForm.invalid) return;
    const v = this.newEntryForm.value;
    const payload: WatchlistEntryRequest = {
      fullName: v.fullName,
      idNumber: v.idNumber || undefined,
      source:   v.source,
      reason:   v.reason || undefined
    };
    this.submit(payload);
  }

  private submit(payload: WatchlistEntryRequest): void {
    this.submitting = true;
    this.submitError = '';
    this.submitSuccess = '';
    this.http.post<WatchlistEntryResponse>(`${this.kycBase}/watchlist`, payload).subscribe({
      next: (entry) => {
        this.entries.unshift(entry);
        this.applyFilter();
        this.submitSuccess = `"${entry.fullName}" has been added to the watchlist.`;
        this.submitting = false;
        this.cdr.detectChanges();   // ← add this
        setTimeout(() => this.closeModal(), 1800);
      },
      error: (err) => {
        this.submitError = err?.error?.message ?? 'Failed to add entry. Please try again.';
        this.submitting = false;
        this.cdr.detectChanges();   // ← add this
      }
    });
  }

  getSeverityClass(source: string): string {
    const map: Record<string, string> = {
      OFAC:         'badge-danger',
      EU_SANCTIONS: 'badge-warning',
      UN_SANCTIONS: 'badge-warning',
      INTERPOL:     'badge-danger',
      LOCAL:        'badge-info',
      OTHER:        'badge-secondary'
    };
    return map[source] ?? 'badge-secondary';
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
