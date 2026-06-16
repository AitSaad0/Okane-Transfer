import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-compliance-sar',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './compliance-sar.component.html',
  styleUrl: './compliance-sar.component.css'
})
export class ComplianceSarComponent implements OnInit {
  sars = signal<any[]>([]);
  loadingList = signal<boolean>(false);
  loadingDetail = signal<boolean>(false);
  selectedSar = signal<any | null>(null);
  error = signal<string>('');

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadSarReports();
  }

  loadSarReports(): void {
    this.loadingList.set(true);
    this.http.get<any[]>('/api/v1/admin/compliance/sar').subscribe({
      next: (data: any[]) => {
        this.sars.set(data || []);
        this.loadingList.set(false);
      },
      error: () => {
        this.error.set('Erreur lors du chargement des rapports SAR');
        this.loadingList.set(false);
      }
    });
  }

  viewSar(id: any): void {
    this.loadingDetail.set(true);
    this.http.get<any>(`/api/v1/admin/compliance/sar/${id}`).subscribe({
      next: (data: any) => {
        this.selectedSar.set(data);
        this.loadingDetail.set(false);
      },
      error: () => {
        this.error.set('Erreur lors du chargement du détail du rapport');
        this.loadingDetail.set(false);
      }
    });
  }

  backToList(): void {
    this.selectedSar.set(null);
    this.error.set('');
  }

  processSar(id: any): void {
    // No /process endpoint exists — just reload the list
    if (this.selectedSar() && this.selectedSar().id === id) {
      this.viewSar(id);
    } else {
      this.loadSarReports();
    }
  }
}
