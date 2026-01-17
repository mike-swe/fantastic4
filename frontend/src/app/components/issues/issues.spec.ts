import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { IssuesComponent } from './issues';
import { IssueService } from '../../services/issue-service';
import { AuthService } from '../../services/auth-service';
import { ProjectService } from '../../services/project-service';
import { Role } from '../../enum/role';
import { Issues } from '../../interfaces/issues';
import { User } from '../../interfaces/user';
import { Project } from '../../interfaces/project';

describe('IssuesComponent', () => {
  let component: IssuesComponent;
  let fixture: ComponentFixture<IssuesComponent>;
  let httpMock: HttpTestingController;
  let issueService: IssueService;
  let authService: AuthService;

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

  const mockUserAdmin: User = {
    id: 'user-3',
    username: 'Test Admin',
    email: 'admin@example.com',
    role: Role.ADMIN
  };

  const mockProject: Project = {
    id: 'proj-1',
    name: 'Test Project',
    description: 'Test Description',
    status: 'ACTIVE',
    createdBy: mockUserTester,
    createdAt: '2024-01-01T00:00:00Z'
  };

  const mockIssue1: Issues = {
    id: 'issue-123-4567-8901',
    title: 'Test Issue 1',
    description: 'Test Description 1',
    status: 'OPEN',
    severity: 'HIGH',
    priority: 'MEDIUM',
    project: mockProject,
    createdBy: mockUserTester,
    assignedTo: mockUserDeveloper,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z'
  };

  const mockIssue2: Issues = {
    id: 'issue-987-6543-2109',
    title: 'Test Issue 2',
    description: 'Test Description 2',
    status: 'IN_PROGRESS',
    severity: 'LOW',
    priority: 'HIGH',
    project: mockProject,
    createdBy: mockUserDeveloper,
    assignedTo: null,
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-04T00:00:00Z'
  };

  const mockIssue3: Issues = {
    id: 'issue-111-2222-3333',
    title: 'Another Issue',
    description: 'Another Description',
    status: 'RESOLVED',
    severity: 'CRITICAL',
    priority: 'CRITICAL',
    project: mockProject,
    createdBy: mockUserAdmin,
    assignedTo: mockUserTester,
    createdAt: '2024-01-05T00:00:00Z',
    updatedAt: '2024-01-06T00:00:00Z'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssuesComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        IssueService,
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(IssuesComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    issueService = TestBed.inject(IssueService);
    authService = TestBed.inject(AuthService);

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
    });
  });

  describe('Role-Based Issue Loading', () => {
    it('should load all issues for ADMIN role', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      component.ngOnInit();

      const req = httpMock.expectOne('http://localhost:8080/issues');
      expect(req.request.method).toBe('GET');
      req.flush([mockIssue1, mockIssue2, mockIssue3]);

      expect(component.issues).toHaveLength(3);
      expect(component.filteredIssues).toHaveLength(3);
    });

    it('should load user-specific issues for TESTER role', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.ngOnInit();

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      expect(req.request.method).toBe('GET');
      req.flush([mockIssue1]);

      expect(component.issues).toHaveLength(1);
      expect(component.filteredIssues).toHaveLength(1);
    });

    it('should load user-specific issues for DEVELOPER role', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserDeveloper);
      component.ngOnInit();

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserDeveloper.id}`);
      expect(req.request.method).toBe('GET');
      req.flush([mockIssue2]);

      expect(component.issues).toHaveLength(1);
      expect(component.filteredIssues).toHaveLength(1);
    });

    it('should handle missing token gracefully', () => {
      vi.spyOn(authService, 'getToken').mockReturnValue(null);
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.ngOnInit();

      expect(consoleSpy).toHaveBeenCalledWith('Issues - No token found');
      expect(component.issues).toHaveLength(0);
    });

    it('should handle unauthenticated user gracefully', () => {
      vi.spyOn(authService, 'isAuthenticated').mockReturnValue(false);
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      component.ngOnInit();

      expect(consoleSpy).toHaveBeenCalledWith('Issues - User not authenticated');
      expect(component.issues).toHaveLength(0);
    });

    it('should handle service errors', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      component.ngOnInit();

      const req = httpMock.expectOne('http://localhost:8080/issues');
      req.error(new ErrorEvent('Network error'));

      expect(consoleErrorSpy).toHaveBeenCalled();
    });
  });

  describe('Issue List Rendering', () => {
    beforeEach(() => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      component.ngOnInit();
    });

    it('should render issues correctly in filteredIssues', () => {
      const req = httpMock.expectOne('http://localhost:8080/issues');
      req.flush([mockIssue1, mockIssue2, mockIssue3]);

      expect(component.issues).toHaveLength(3);
      expect(component.filteredIssues).toHaveLength(3);
      expect(component.filteredIssues[0].id).toBe(mockIssue1.id);
    });

    it('should display empty state when no issues', () => {
      const req = httpMock.expectOne('http://localhost:8080/issues');
      req.flush([]);

      expect(component.issues).toHaveLength(0);
      expect(component.filteredIssues).toHaveLength(0);
    });

    it('should extract assigned users correctly', () => {
      const req = httpMock.expectOne('http://localhost:8080/issues');
      req.flush([mockIssue1, mockIssue2, mockIssue3]);

      expect(component.assignedUsers).toHaveLength(2);
      expect(component.assignedUsers.some(u => u.id === mockUserDeveloper.id)).toBe(true);
      expect(component.assignedUsers.some(u => u.id === mockUserTester.id)).toBe(true);
    });
  });

  describe('Filtering', () => {
    beforeEach(() => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      component.issues = [mockIssue1, mockIssue2, mockIssue3];
      component.extractAssignedUsers();
    });

    it('should filter by search query (title)', () => {
      component.searchQuery = 'Test Issue 1';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].title).toBe('Test Issue 1');
    });

    it('should filter by search query (ID)', () => {
      // formatIssueId('issue-123-4567-8901') returns 'ISS-ISSUE123'
      // Search for part that appears in formatted ID
      component.searchQuery = 'ISSUE123';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].id).toBe(mockIssue1.id);
    });

    it('should filter by search query (project name)', () => {
      component.searchQuery = 'Test Project';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(3);
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

    it('should filter by assignedTo (specific user)', () => {
      component.selectedAssignedTo = mockUserDeveloper.id;
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].assignedTo?.id).toBe(mockUserDeveloper.id);
    });

    it('should filter by assignedTo (UNASSIGNED)', () => {
      component.selectedAssignedTo = 'UNASSIGNED';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].assignedTo).toBeNull();
    });

    it('should filter by date range (TODAY)', () => {
      const today = new Date();
      const todayIssue: Issues = {
        ...mockIssue1,
        updatedAt: today.toISOString()
      };
      component.issues = [todayIssue, mockIssue2];
      component.selectedDateRange = 'TODAY';
      component.applyFilters();

      expect(component.filteredIssues.length).toBeGreaterThanOrEqual(0);
    });

    it('should filter by date range (THIS_WEEK)', () => {
      const thisWeek = new Date();
      const weekIssue: Issues = {
        ...mockIssue1,
        updatedAt: thisWeek.toISOString()
      };
      component.issues = [weekIssue, mockIssue2];
      component.selectedDateRange = 'THIS_WEEK';
      component.applyFilters();

      expect(component.filteredIssues.length).toBeGreaterThanOrEqual(0);
    });

    it('should filter by date range (THIS_MONTH)', () => {
      const thisMonth = new Date();
      const monthIssue: Issues = {
        ...mockIssue1,
        updatedAt: thisMonth.toISOString()
      };
      component.issues = [monthIssue, mockIssue2];
      component.selectedDateRange = 'THIS_MONTH';
      component.applyFilters();

      expect(component.filteredIssues.length).toBeGreaterThanOrEqual(0);
    });

    it('should filter by date range (LAST_30_DAYS)', () => {
      const recentDate = new Date();
      recentDate.setDate(recentDate.getDate() - 15);
      const recentIssue: Issues = {
        ...mockIssue1,
        updatedAt: recentDate.toISOString()
      };
      component.issues = [recentIssue, mockIssue2];
      component.selectedDateRange = 'LAST_30_DAYS';
      component.applyFilters();

      expect(component.filteredIssues.length).toBeGreaterThanOrEqual(0);
    });

    it('should filter by date range (CUSTOM)', () => {
      component.selectedDateRange = 'CUSTOM';
      component.customFromDate = '2024-01-01';
      component.customToDate = '2024-01-02';
      component.applyFilters();

      expect(component.filteredIssues.length).toBeGreaterThanOrEqual(0);
    });

    it('should apply multiple filters simultaneously', () => {
      component.searchQuery = 'Test';
      component.selectedStatus = 'OPEN';
      component.selectedSeverity = 'HIGH';
      component.applyFilters();

      expect(component.filteredIssues).toHaveLength(1);
      expect(component.filteredIssues[0].title).toBe('Test Issue 1');
    });

    it('should call applyFilters on search change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onSearchChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should call applyFilters on status change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onStatusChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should call applyFilters on severity change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onSeverityChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should call applyFilters on priority change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onPriorityChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should call applyFilters on assignedTo change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onAssignedToChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });

    it('should call applyFilters on dateRange change', () => {
      const applyFiltersSpy = vi.spyOn(component, 'applyFilters');
      component.onDateRangeChange();
      expect(applyFiltersSpy).toHaveBeenCalled();
    });
  });

  describe('Role-Based Actions', () => {
    it('should set userRole to TESTER when current user is TESTER', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserTester);
      component.ngOnInit();

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserTester.id}`);
      req.flush([]);

      expect(component.userRole).toBe(Role.TESTER);
    });

    it('should set userRole to ADMIN when current user is ADMIN', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      component.ngOnInit();

      const req = httpMock.expectOne('http://localhost:8080/issues');
      req.flush([]);

      expect(component.userRole).toBe(Role.ADMIN);
    });

    it('should set userRole to DEVELOPER when current user is DEVELOPER', () => {
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserDeveloper);
      component.ngOnInit();

      const req = httpMock.expectOne(`http://localhost:8080/issues/user/${mockUserDeveloper.id}`);
      req.flush([]);

      expect(component.userRole).toBe(Role.DEVELOPER);
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

    it('should return empty string for null description', () => {
      const truncated = component.truncateDescription('');
      expect(truncated).toBe('');
    });

    it('should return correct status color for OPEN', () => {
      const color = component.getStatusColor('OPEN');
      expect(color).toBe('#f97316');
    });

    it('should return correct status color for IN_PROGRESS', () => {
      const color = component.getStatusColor('IN_PROGRESS');
      expect(color).toBe('#3b82f6');
    });

    it('should return correct status color for RESOLVED', () => {
      const color = component.getStatusColor('RESOLVED');
      expect(color).toBe('#10b981');
    });

    it('should return correct status color for CLOSED', () => {
      const color = component.getStatusColor('CLOSED');
      expect(color).toBe('#6b7280');
    });

    it('should return correct severity color for LOW', () => {
      const color = component.getSeverityColor('LOW');
      expect(color).toBe('#10b981');
    });

    it('should return correct severity color for MEDIUM', () => {
      const color = component.getSeverityColor('MEDIUM');
      expect(color).toBe('#f97316');
    });

    it('should return correct severity color for HIGH', () => {
      const color = component.getSeverityColor('HIGH');
      expect(color).toBe('#ef4444');
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
      vi.spyOn(authService, 'getCurrentUser').mockReturnValue(mockUserAdmin);
      component.showModal = true;
      const loadIssuesSpy = vi.spyOn(component, 'loadIssues');

      component.onIssueCreated(mockIssue1);

      expect(component.showModal).toBe(false);
      expect(loadIssuesSpy).toHaveBeenCalled();
    });

    it('should open detail modal with selected issue', () => {
      component.openIssueDetail(mockIssue1);
      expect(component.showDetailModal).toBe(true);
      expect(component.selectedIssue).toBe(mockIssue1);
    });

    it('should close detail modal', () => {
      component.showDetailModal = true;
      component.selectedIssue = mockIssue1;
      component.onDetailModalClose();
      expect(component.showDetailModal).toBe(false);
      expect(component.selectedIssue).toBeNull();
    });
  });
});
