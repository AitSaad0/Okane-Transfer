import { Component, OnInit } from '@angular/core';
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
  createdAt: string;
}

@Component({
  selector: 'app-compliance-sar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compliance-sar.component.html',
  styleUrls: ['./compliance-sar.component.css']
})
export class ComplianceSarComponent implements OnInit {
  sars: SarDto[] = [];
  selectedSar: SarDto | null = null;
  loading = false;
  error: string | null = null;

  private readonly baseUrl = '/api/v1/admin/compliance/sar';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadSars();
  }

  loadSars(): void {
    this.loading = true;
    this.error = null;
    this.http.get<SarDto[]>(this.baseUrl).subscribe({
      next: (data) => {
        this.sars = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load SAR reports.';
        this.loading = false;
      }
    });
  }

  viewSar(id: number): void {
    this.loading = true;
    this.error = null;
    this.http.get<SarDto>(`${this.baseUrl}/${id}`).subscribe({
      next: (data) => {
        this.selectedSar = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load SAR detail.';
        this.loading = false;
      }
    });
  }

  backToList(): void {
    this.selectedSar = null;
  }
}
