import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface SarDto {
  id: number;
  referenceCode: string;
  reason: string;
  thresholdAmount: number;
  transferAmount: number;
  status: string;
  transferCode: string;
  createdAt: Date;  // ← Date, not string
}

@Component({
  selector: 'app-compliance-sar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compliance-sar.component.html',
  styleUrls: ['./compliance-sar.component.css']
})
export class ComplianceSarComponent implements OnInit {
  sars = signal<SarDto[]>([]);
  selectedSar = signal<SarDto | null>(null);
  loadingList = signal(false);
  loadingDetail = signal(false);
  error = signal<string | null>(null);

  private readonly baseUrl = '/api/v1/admin/compliance/sar';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadSars();
  }

  private parseCreatedAt(value: any): Date {
    if (Array.isArray(value)) {
      const [year, month, day, hour, min, sec] = value;
      return new Date(year, month - 1, day, hour, min, sec);
    }
    return new Date(value);
  }

  private mapSar(raw: any): SarDto {
    return { ...raw, createdAt: this.parseCreatedAt(raw.createdAt) };
  }

  loadSars(): void {
    this.loadingList.set(true);
    this.error.set(null);
    this.http.get<any>(this.baseUrl).subscribe({
      next: (data) => {
        console.log('NEXT REACHED', data);
        try {
          // gère le cas où le backend renvoie un objet Page<> au lieu d'un tableau brut
          const list = Array.isArray(data) ? data : (data?.content ?? []);
          this.sars.set(list.map((s: any) => this.mapSar(s)));
        } catch (e) {
          console.error('Mapping error:', e, data);
          this.error.set('Erreur lors du traitement des données.');
        } finally {
          this.loadingList.set(false);
        }
      },
      error: (err) => {
        console.error('SAR error:', err);
        this.error.set('Failed to load SAR reports.');
        this.loadingList.set(false);
      }
    });
  }

  viewSar(id: number): void {
    this.loadingDetail.set(true);
    this.error.set(null);

    this.http.get<any>(`${this.baseUrl}/${id}`).subscribe({
      next: (data) => {
        try {
          this.selectedSar.set(this.mapSar(data));
        } catch (e) {
          console.error('Mapping error:', e, data);
          this.error.set('Erreur lors du traitement du détail.');
        } finally {
          this.loadingDetail.set(false);
        }
      },
      error: (err) => {
        console.error('SAR detail error:', err);
        this.error.set('Failed to load SAR detail.');
        this.loadingDetail.set(false);
      }
    });
  }

  backToList(): void {
    this.selectedSar.set(null);
  }
}
