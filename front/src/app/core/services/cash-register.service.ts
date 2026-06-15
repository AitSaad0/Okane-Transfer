import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Caisse, ClotureCaisseRequest, EcartCaisseRequest, OperationCaisse } from '../../pages/agent/models/caisse.model';

@Injectable({ providedIn: 'root' })
export class CashRegisterService {
  // CORRECTION : Changer l'URL de base
  private readonly base = '/api/v1/agent/cash-register';  // Au lieu de '/api/v1/caisse'

  constructor(private http: HttpClient) {}

  getMyCaisse(): Observable<Caisse> {
    // CORRECTION : Enlever /current
    return this.http.get<Caisse>(`${this.base}`);
  }

  getOperations(): Observable<OperationCaisse[]> {
    // CORRECTION : Ajouter /operations
    return this.http.get<OperationCaisse[]>(`${this.base}/operations`);
  }

  closeCaisse(req: ClotureCaisseRequest): Observable<Caisse> {
    // CORRECTION : Utiliser /close au lieu de /cloturer
    return this.http.post<Caisse>(`${this.base}/close`, req);
  }

  declareDiscrepancy(req: EcartCaisseRequest): Observable<void> {
    // CORRECTION : Utiliser /discrepancy au lieu de /ecart
    return this.http.post<void>(`${this.base}/discrepancy`, req);
  }
}
