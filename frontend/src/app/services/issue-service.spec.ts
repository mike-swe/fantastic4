import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Issues } from '../interfaces/issues';
import { IssueHistory } from '../interfaces/issue-history';
import { IssueService } from './issue-service';
import { firstValueFrom } from 'rxjs';
import { User } from '../interfaces/user';
import { Project } from '../interfaces/project';
import { Role } from '../enum/role';


describe('IssueService', () => {
  let service: IssueService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080'

  const mockUser: User = {
    id: 'user-123',
    username: 'test-user',
    email: 'test@example.com',
    role: Role.TESTER,
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z'
    };
    /*
    id: string;
    name: string;
    description: string;
    status: 'ACTIVE' | 'ARCHIVED';
    createdBy: User;
    createdAt: string;
    issues ?: Issues[];
    assignments ?: any[]; 
    */

  const mockProject: Project = {
    id : 'pro-id',
    name : 'pro-id',
    description : 'prodesc',
    status: 'ACTIVE',
    createdBy: mockUser,
    createdAt: '2024-01-01T10:00:00Z',
    issues: [],
    assignments: [] 
  }
  /* 
    Issues
    id: string;
    title: string;
    description: string;
    status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
    severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    project: Project;  
    createdBy: User;
    assignedTo?: User | null;
    createdAt: string;
    updatedAt?: string;
    comments?: Comment[];
  */
  const mockIssue: Issues = {
    id: 'iss-101',
    title: 'Bug in Login',
    description: 'User cannot login with valid credentials',
    status: 'OPEN',
    severity: 'HIGH',
    priority: 'HIGH',
    project: mockProject,
    createdBy: mockUser,
    createdAt: '2026-01-15T10:00:00Z'
  };
  /*
    IssueHistory
    id: string;
    issue: Issues;
    changedByUser: User;
    changedAt: string;
    fieldName: 'TITLE' | 'DESCRIPTION' | 'STATUS' | 'SEVERITY' | 'PRIORITY' | null;
    oldValue: string | null;
    newValue: string | null;
    changeType: 'CREATED' | 'STATUS_CHANGE' | 'FIELD_UPDATE';

  */

  const mockHistory: IssueHistory = {
    id: 'hist-1',
    issue: mockIssue,
    changedByUser: mockUser,
    changedAt: '2026-01-15T11:00:00Z',
    fieldName: 'STATUS',
    oldValue: 'OPEN',
    newValue: 'IN_PROGRESS',
    changeType: 'STATUS_CHANGE'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        IssueService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(IssueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });


  it('should fetch all issues', () => {
    service.getAllIssues().subscribe();
    const req = httpMock.expectOne(`${baseUrl}/issues`);
    expect(req.request.method).toBe('GET');
  });

  it('should fetch issues by user', () => {
    service.getIssuesByUser('u123').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/issues/user/u123`);
    expect(req.request.method).toBe('GET');
  });

  it('should create an issue with the correct body', () => {
    const newIssue: any = { title: 'Test' };
    service.createIssue(newIssue).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/issues`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newIssue);
  });

  it('should update status with wrapped body', () => {
    service.updateIssueStatus('iss-1', 'RESOLVED').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/issues/iss-1/status`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ status: 'RESOLVED' });
  });

  it('should fetch assigned issues', () => {
    service.getAssignedIssues('dev-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/issues/assigned/dev-1`);
    expect(req.request.method).toBe('GET');
  });

  it('should fetch history for an issue', () => {
    service.getIssueHistory('iss-1').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/issues/iss-1/history`);
    expect(req.request.method).toBe('GET');
  });
});

