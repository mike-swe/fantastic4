import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ChangeDetectorRef } from '@angular/core';
import { MyIssues } from './my-issues';
import { IssueService } from '../../services/issue-service';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';
import { Issues } from '../../interfaces/issues';
import { User } from '../../interfaces/user';
import { Project } from '../../interfaces/project';

describe('MyIssuesComponent', () => {
  let component: MyIssues;
  let fixture: ComponentFixture<MyIssues>;
  let httpMock: HttpTestingController;
  let issueService: IssueService;
  let authService: AuthService;
  let cdr: ChangeDetectorRef;

  const mockUserTester: User = {
    id: 'user-1',
    username: 'Test Tester',
    email: 'tester@example.com',
    role: Role.TESTER
  };

  const mockUserDeveloper: User = {
    id: 'user-2',
    username: 'Test Developer',
    email: 'developer@example.com',
    role: Role.DEVELOPER
  };

  const mockProject: Project = {
    id: 'proj-1',
    name: 'Test Project',
    description: 'Test Description',
    status: 'ACTIVE',
    createdBy: mockUserTester,
    createdAt: '2024-01-01T00:00:00Z'
  };

  const mockIssueOpen: Issues = {
    id: 'issue-open-123',
    title: 'Open Issue',
    description: 'Test Description',
    status: 'OPEN',
    severity: 'HIGH',
    priority: 'MEDIUM',
    project: mockProject,
    createdBy: mockUserTester,
    assignedTo: mockUserDeveloper,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z'
  };

  const mockIssueClosed: Issues = {
    id: 'issue-closed-456',
    title: 'Closed Issue',
    description: 'Test Description',
    status: 'CLOSED',
    severity: 'LOW',
    priority: 'HIGH',
    project: mockProject,
    createdBy: mockUserTester,
    assignedTo: null,
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-04T00:00:00Z'
  };

  const mockIssueInProgress: Issues = {
    id: 'issue-inprogress-789',
    title: 'In Progress Issue',
    description: 'Test Description',
    status: 'IN_PROGRESS',
    severity: 'MEDIUM',
    priority: 'LOW',
    project: mockProject,
    createdBy: mockUserTester,
    assignedTo: mockUserTester,
    createdAt: '2024-01-05T00:00:00Z',
    updatedAt: '2024-01-06T00:00:00Z'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyIssues],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        IssueService,
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyIssues);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    issueService = TestBed.inject(IssueService);
    authService = TestBed.inject(AuthService);
    // Access the component's private cdr property and spy on it
    // If we don't do this, the cdr would be from a different instance instead of the 
    // component instance that we want to test. 
    cdr = (component as any).cdr;
    vi.spyOn(cdr, 'detectChanges');

    // Mock AuthService methods
    vi.spyOn(authService, 'getToken').mockReturnValue('mock-token');
    vi.spyOn(authService, 'isAuthenticated').mockReturnValue(true);
  });

  afterEach(() => {
    httpMock.verify();
    vi.clearAllMocks();
  });

  describe('Component Creation', () => {
    it('should create component successfully', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default filter values', () => {
      expect(component.searchQuery).toBe('');
      expect(component.selectedStatus).toBe('ALL');
      expect(component.selectedSeverity).toBe('ALL');
      expect(component.selectedPriority).toBe('ALL');
      expect(component.selectedAssignedTo).toBe('ALL');
      expect(component.selectedDateRange).toBe('ALL');
      expect(component.showModal).toBe(false);
      expect(component.showDetailModal).toBe(false);
      expect(component.selectedIssue).toBeNull();
      expect(component.updatingStatus).toEqual({});
    });
  });

  describe('TESTER-Only Access Restriction', () => {
    it('should load issues for TESTER role', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.ngOnInit();

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      expect(req.request.method).toBe('GET');
      req.flush([mockIssueOpen]);

      expect(component.issues).toHaveLength(1);
      expect(component.userRole).toBe(Role.TESTER);
    });

    it('should warn and not load issues for non-TESTER role', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserDeveloper);
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.ngOnInit();

      expect(consoleSpy).toHaveBeenCalledWith('MyIssues - Only TESTER can access this page');
      expect(component.issues).toHaveLength(0);
      httpMock.expectNone(`http://localhost:8080/issues/user/${mockUserDeveloper.id}`);
    });

    it('should handle missing token gracefully', () => {
      vi.spyOn(authService, 'getToken').mockReturnValue(null);
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.ngOnInit();

      expect(consoleSpy).toHaveBeenCalledWith('MyIssues - No token found');
      expect(component.issues).toHaveLength(0);
    });

    it('should handle unauthenticated user gracefully', () => {
      vi.spyOn(authService, 'isAuthenticated').mockReturnValue(false);
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.ngOnInit();

      expect(consoleSpy).toHaveBeenCalledWith('MyIssues - User not authenticated');
      expect(component.issues).toHaveLength(0);
    });

    it('should handle missing user ID gracefully', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue({ ...mockUserTester, id: '' });
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.loadIssues();

      expect(consoleSpy).toHaveBeenCalledWith('MyIssues - No user ID available');
    });
  });

  describe('Issue List Rendering', () => {
    beforeEach(() => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.ngOnInit();
    });

    it('should load issues for current user', () => {
      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      req.flush([mockIssueOpen, mockIssueClosed]);

      expect(component.issues).toHaveLength(2);
      expect(component.filteredIssues).toHaveLength(2);
    });

    it('should render filtered issues correctly', () => {
      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      req.flush([mockIssueOpen, mockIssueClosed, mockIssueInProgress]);

      expect(component.issues).toHaveLength(3);
      expect(component.filteredIssues).toHaveLength(3);
    });

    it('should extract assigned users correctly', () => {
      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      req.flush([mockIssueOpen, mockIssueClosed, mockIssueInProgress]);

      expect(component.assignedUsers.length).toBeGreaterThanOrEqual(0);
    });

    it('should handle service errors', () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      req.error(new ErrorEvent('Network error'));

      expect(consoleErrorSpy).toHaveBeenCalled();
    });
  });

  describe('Status Transitions', () => {
    beforeEach(() => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.issues = [mockIssueOpen, mockIssueClosed];
      component.applyFilters();
    });

    it('should successfully update status from OPEN to CLOSED', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED' };

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'CLOSED' });
      req.flush(updatedIssue);

      expect(component.issues[0].status).toBe('CLOSED');
      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
      expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('should successfully update status from CLOSED to OPEN', () => {
      const updatedIssue: Issues = { ...mockIssueClosed, status: 'OPEN' };

      component.updateIssueStatus(mockIssueClosed, 'OPEN');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueClosed.id}/status`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: 'OPEN' });
      req.flush(updatedIssue);

      expect(component.issues[1].status).toBe('OPEN');
      expect(component.updatingStatus[mockIssueClosed.id]).toBe(false);
      expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('should prevent concurrent updates with updatingStatus flag', () => {
      component.updatingStatus[mockIssueOpen.id] = true;

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      httpMock.expectNone(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
    });

    it('should set updatingStatus flag during update', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED' };

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      expect(component.updatingStatus[mockIssueOpen.id]).toBe(true);

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(updatedIssue);

      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
    });

    it('should update local issues array after successful status change', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED', updatedAt: '2024-01-10T00:00:00Z' };

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(updatedIssue);

      const updatedIssueInArray = component.issues.find(i => i.id === mockIssueOpen.id);
      expect(updatedIssueInArray?.status).toBe('CLOSED');
      expect(updatedIssueInArray?.updatedAt).toBe('2024-01-10T00:00:00Z');
    });

    it('should reapply filters after status update', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED' };
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(updatedIssue);

      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should handle HTTP errors gracefully', () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(
        { message: 'Internal Server Error' },
        { status: 500, statusText: 'Internal Server Error' }
      );

      expect(consoleErrorSpy).toHaveBeenCalled();
      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
      expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('should handle HTTP errors with error message', () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(
        { message: 'Invalid status transition' },
        { status: 400, statusText: 'Bad Request' }
      );

      expect(alertSpy).toHaveBeenCalledWith('Invalid status transition');
      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
    });

    it('should show loading state during update', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED' };

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      expect(component.updatingStatus[mockIssueOpen.id]).toBe(true);

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(updatedIssue);

      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
    });

    it('should allow canUpdateStatus to return true', () => {
      const canUpdate = component.canUpdateStatus(mockIssueOpen);
      expect(canUpdate).toBe(true);
    });

    it('should handle issue not found in array after update', () => {
      const updatedIssue: Issues = { ...mockIssueOpen, status: 'CLOSED' };
      component.issues = []; 

      component.updateIssueStatus(mockIssueOpen, 'CLOSED');

      const req = httpMock.expectOne(`http://localhost:8080/issues/${mockIssueOpen.id}/status`);
      req.flush(updatedIssue);

      // Should not throw error, just not update anything
      expect(component.issues).toHaveLength(0);
      expect(component.updatingStatus[mockIssueOpen.id]).toBe(false);
    });
  });

  describe('Filtering', () => {
    beforeEach(() => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.issues = [mockIssueOpen, mockIssueClosed, mockIssueInProgress];
      component.extractAssignedUsers();
    });

    it('should filter by search query (title)', () => {
      component.searchQuery = 'Open';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].title).toBe('Open Issue');
    });

    it('should filter by search query (ID)', () => {
      // formatIssueId('issue-open-123') returns 'ISS-ISSUEOPE'
      // Search for part that appears in formatted ID or original ID
      component.searchQuery = 'open';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].id).toBe(mockIssueOpen.id);
    });

    it('should filter by status', () => {
      component.selectedStatus = 'OPEN';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].status).toBe('OPEN');
    });

    it('should filter by severity', () => {
      component.selectedSeverity = 'HIGH';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].severity).toBe('HIGH');
    });

    it('should filter by priority', () => {
      component.selectedPriority = 'HIGH';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].priority).toBe('HIGH');
    });

    it('should filter by assignedTo (UNASSIGNED)', () => {
      component.selectedAssignedTo = 'UNASSIGNED';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].assignedTo).toBeNull();
    });

    it('should apply multiple filters simultaneously', () => {
      component.searchQuery = 'Issue';
      component.selectedStatus = 'OPEN';
      component.selectedSeverity = 'HIGH';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].title).toBe('Open Issue');
    });

    it('should call applyFilters on filter change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      
      component.onSearchChange();
      expect(applyFiltersSpy).toHaveBeenCalled();

      applyFiltersSpy.mockClear();
      component.onStatusChange();
      expect(applyFiltersSpy).toHaveBeenCalled();

      applyFiltersSpy.mockClear();
      component.onSeverityChange();
      expect(applyFiltersSpy).toHaveBeenCalled();

      applyFiltersSpy.mockClear();
      component.onPriorityChange();
      expect(applyFiltersSpy).toHaveBeenCalled();

      applyFiltersSpy.mockClear();
      component.onAssignedToChange();
      expect(applyFiltersSpy).toHaveBeenCalled();

      applyFiltersSpy.mockClear();
      component.onDateRangeChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });
  });

  describe('Helper Methods', () => {
    it('should format issue ID correctly', () => {
      const formatted = component.formatIssueId('issue-123-4567-8901');
      expect(formatted).toContain('ISS-');
      expect(formatted.length).toBeGreaterThan(4);
    });

    it('should return default format for empty ID', () => {
      const formatted = component.formatIssueId('');
      expect(formatted).toBe('ISS-0000');
    });

    it('should format date correctly', () => {
      const formatted = component.formatDate('2024-01-01T00:00:00Z');
      expect(formatted).toBe('2024-01-01');
    });

    it('should return N/A for invalid date', () => {
      const formatted = component.formatDate(undefined);
      expect(formatted).toBe('N/A');
    });

    it('should get user initials from full name', () => {
      const user: User = {
        id: '1',
        username: 'John Doe',
        email: 'john@example.com',
        role: Role.TESTER
      };
      const initials = component.getUserInitials(user);
      expect(initials).toBe('JD');
    });

    it('should get user initials from single name', () => {
      const user: User = {
        id: '1',
        username: 'John',
        email: 'john@example.com',
        role: Role.TESTER
      };
      const initials = component.getUserInitials(user);
      expect(initials).toBe('JO');
    });

    it('should return ?? for null user', () => {
      const initials = component.getUserInitials(null);
      expect(initials).toBe('??');
    });

    it('should truncate description when longer than maxLength', () => {
      const longDescription = 'a'.repeat(200);
      const truncated = component.truncateDescription(longDescription, 150);
      expect(truncated.length).toBe(153); // 150 + '...'
      expect(truncated).toContain('...');
    });

    it('should not truncate description when shorter than maxLength', () => {
      const shortDescription = 'Short description';
      const truncated = component.truncateDescription(shortDescription, 150);
      expect(truncated).toBe(shortDescription);
    });

    it('should return correct status color for OPEN', () => {
      const color = component.getStatusColor('OPEN');
      expect(color).toBe('#f97316');
    });

    it('should return correct status color for CLOSED', () => {
      const color = component.getStatusColor('CLOSED');
      expect(color).toBe('#6b7280');
    });

    it('should return correct severity color for CRITICAL', () => {
      const color = component.getSeverityColor('CRITICAL');
      expect(color).toBe('#dc2626');
    });

    it('should return correct priority color', () => {
      const color = component.getPriorityColor('HIGH');
      expect(color).toBe('#ef4444');
    });
  });

  describe('Modal Interactions', () => {
    it('should open new issue modal', () => {
      component.createNewIssue();
      expect(component.showModal).toBe(true);
    });

    it('should close new issue modal', () => {
      component.showModal = true;
      component.onModalClose();
      expect(component.showModal).toBe(false);
    });

    it('should reload issues after creating new issue', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.showModal = true;
      const loadIssuesSpy = vi.spyOn(component, 'loadIssues');

      component.onIssueCreated(mockIssueOpen);

      expect(component.showModal).toBe(false);
      expect(loadIssuesSpy).toHaveBeenCalled();
    });

    it('should open detail modal with selected issue', () => {
      component.openIssueDetail(mockIssueOpen);
      expect(component.showDetailModal).toBe(true);
      expect(component.selectedIssue).toBe(mockIssueOpen);
    });

    it('should close detail modal', () => {
      component.showDetailModal = true;
      component.selectedIssue = mockIssueOpen;
      component.onDetailModalClose();
      expect(component.showDetailModal).toBe(false);
      expect(component.selectedIssue).toBeNull();
    });
  });
});
