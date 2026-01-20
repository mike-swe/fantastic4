import { TestBed } from '@angular/core/testing';

import { AuditService } from './audit-service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuditLog } from '../interfaces/audit-log';
import { provideHttpClient } from '@angular/common/http';

import { firstValueFrom } from 'rxjs';
import { Type } from '@angular/core';


describe('AuditService', () => {
  let service: AuditService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080'

    /*
    id: number;
    actorUserId: string;
    action: string;
    entityType: string;
    entityId: string;
    timestamp: string;
    details: string;

  */
  const mockData: AuditLog[] = [
    { 
      id: 1, 
      actorUserId: 'admin-001', 
      action: 'CREATE', 
      entityType: 'USER', 
      entityId: '101', 
      timestamp: '2024-05-20T10:00:00Z', 
      details: 'Created new user' 
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuditService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuditService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

    describe("get Audit Logs", () => {

      it('should fetch all audit logs', () => {
        service.getAllAuditLogs().subscribe(logs => {
          expect(logs.length).toBe(1);
          expect(logs).toEqual(mockData);
      });

        const req = httpMock.expectOne(`${baseUrl}/audit`);
        expect(req.request.method).toBe('GET');
        req.flush(mockData);
      });

      it('should fetch logs by entity type', async () => {
        const type = 'USER';

        const promise = firstValueFrom(service.getAuditLogsByEntityType(type));

        const req = httpMock.expectOne(`${baseUrl}/audit/entity/${type}`);
        expect(req.request.method).toBe('GET');

        req.flush([mockData[0]]);
        
        const logs = await promise;
        
        expect(logs[0].entityType).toBe(type)
      });

      it('should fetch logs by actor ID', async () => {
        const actorId = 'admin-001';

        // 1. Create a promise that waits for the first value from the observable
        const logsPromise = firstValueFrom(service.getAuditLogsByActor(actorId));

        // 2. Mock the HTTP response as usual
        const req = httpMock.expectOne(`${baseUrl}/audit/actor/${actorId}`);
        expect(req.request.method).toBe('GET');
        req.flush([mockData[0]]);

        // 3. Await the promise to get the actual data
        const logs = await logsPromise;

        // 4. NOW the expectation is on the main timeline. 
        // If this fails, the UI will correctly show a failure!
        expect(logs[0].actorUserId).toBe('admin-001'); 
      });


    });
  
});
