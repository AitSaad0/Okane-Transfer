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
  
  // Signaux réclamés par le template HTML
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
    this.http.get<any[]>('/api/compliance/sars').subscribe({
      next: (data: any[]) => {
        this.sars.set(data || []);
        this.loadingList.set(false);
      },
      error: (err: any) => {
        this.error.set('Erreur lors du chargement des rapports SAR');
        this.loadingList.set(false);
      }
    });
  }

  viewSar(id: any): void {
    this.loadingDetail.set(true);
    this.http.get<any>(`/api/compliance/sars/${id}`).subscribe({
      next: (data: any) => {
        this.selectedSar.set(data);
        this.loadingDetail.set(false);
      },
      error: (err: any) => {
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
    this.http.post(`/api/compliance/sars/${id}/process`, {}).subscribe({
      next: () => {
        // Si on est en mode détail, on rafraîchit le détail, sinon la liste
        if (this.selectedSar() && this.selectedSar().id === id) {
          this.viewSar(id);
        } else {
          this.loadSarReports();
        }
      },
      error: (err: any) => {
        this.error.set('Erreur lors du traitement du rapport');
      }
    });
  }
}
