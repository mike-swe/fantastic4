import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditLog } from '../interfaces/audit-log';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})
export class AuditService {

  constructor(private http: HttpClient) {}

  getAllAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${API_BASE_URL}/audit`);
  }

  getAuditLogsByEntityType(entityType: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${API_BASE_URL}/audit/entity/${entityType}`);
  }

  getAuditLogsByActor(actorId: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${API_BASE_URL}/audit/actor/${actorId}`);
  }
}

