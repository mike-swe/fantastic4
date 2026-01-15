import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Dashboard } from './dashboard';
import { ProjectService } from '../../services/project-service';
import { IssueService } from '../../services/issue-service';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { Issues } from '../../interfaces/issues';
import { User } from '../../interfaces/user';

describe('Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let mockAuthService: {
    getToken: ReturnType<typeof vi.fn>;
    isAuthenticated: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
  };
  let mockProjectService: {
    getAllProjects: ReturnType<typeof vi.fn>;
    getUserProjects: ReturnType<typeof vi.fn>;
  };
  let mockIssueService: {
    getAllIssues: ReturnType<typeof vi.fn>;
    getIssuesByUser: ReturnType<typeof vi.fn>;
    getAssignedIssues: ReturnType<typeof vi.fn>;
  };
  let mockChangeDetectorRef: {
    detectChanges: ReturnType<typeof vi.fn>;
  };
  let consoleLogSpy: ReturnType<typeof vi.spyOn>;
  let consoleWarnSpy: ReturnType<typeof vi.spyOn>;

  const mockAdminUser: User = {
    id: '1',
    username: 'admin',
    email: 'admin@test.com',
    role: Role.ADMIN
  };

  const mockTesterUser: User = {
    id: '2',
    username: 'tester',
    email: 'tester@test.com',
    role: Role.TESTER
  };

  const mockDeveloperUser: User = {
    id: '3',
    username: 'developer',
    email: 'developer@test.com',
    role: Role.DEVELOPER
  };

  const mockProjects: Project[] = [
    {
      id: '1',
      name: 'Project 1',
      description: 'Description 1',
      status: 'ACTIVE',
      createdBy: mockAdminUser,
      createdAt: '2024-01-01',
      issues: []
    },
    {
      id: '2',
      name: 'Project 2',
      description: 'Description 2',
      status: 'ARCHIVED',
      createdBy: mockAdminUser,
      createdAt: '2024-01-02',
      issues: [{ id: '1', title: 'Issue 1' } as Issues]
    }
  ];

  const mockIssues: Issues[] = [
    {
      id: '1',
      title: 'Issue 1',
      description: 'Description 1',
      status: 'OPEN',
      severity: 'HIGH',
      priority: 'HIGH',
      project: mockProjects[0],
      createdBy: mockTesterUser,
      assignedTo: mockDeveloperUser,
      createdAt: '2024-01-01'
    },
    {
      id: '2',
      title: 'Issue 2',
      description: 'Description 2',
      status: 'IN_PROGRESS',
      severity: 'MEDIUM',
      priority: 'MEDIUM',
      project: mockProjects[1],
      createdBy: mockTesterUser,
      assignedTo: null,
      createdAt: '2024-01-02'
    }
  ];

  beforeEach(async () => {
    consoleLogSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

    mockAuthService = {
      getToken: vi.fn(),
      isAuthenticated: vi.fn(),
      getCurrentUser: vi.fn()
    };

    mockProjectService = {
      getAllProjects: vi.fn(),
      getUserProjects: vi.fn()
    };

    mockIssueService = {
      getAllIssues: vi.fn(),
      getIssuesByUser: vi.fn(),
      getAssignedIssues: vi.fn()
    };

    mockChangeDetectorRef = {
      detectChanges: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: IssueService, useValue: mockIssueService },
        { provide: ChangeDetectorRef, useValue: mockChangeDetectorRef }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Data Loading - Authentication', () => {
    it('should not load data when no token is present', () => {
      mockAuthService.getToken.mockReturnValue(null);
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(mockIssueService.getAllIssues).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(component.issues).toEqual([]);
    });

    it('should not load data when user is not authenticated', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(mockIssueService.getAllIssues).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(component.issues).toEqual([]);
    });

    it('should not load data when current user is null', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(null);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(mockIssueService.getAllIssues).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(component.issues).toEqual([]);
    });

    it('should not load data when user has no role', () => {
      const userWithoutRole = { ...mockAdminUser, role: undefined as any };
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(userWithoutRole);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(mockIssueService.getAllIssues).not.toHaveBeenCalled();
    });
  });

  describe('Data Loading - Projects by Role', () => {
    it('should load all projects for ADMIN role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
      mockIssueService.getAllIssues.mockReturnValue(of([]));

      fixture.detectChanges();
      await fixture.whenStable();
      
      expect(mockProjectService.getAllProjects).toHaveBeenCalled();
      expect(component.projects).toEqual(mockProjects);
    });

    it('should load user projects for TESTER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));
      mockIssueService.getIssuesByUser.mockReturnValue(of([]));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProjectService.getUserProjects).toHaveBeenCalledWith(mockTesterUser.id);
      expect(component.projects).toEqual(mockProjects);
    });

    it('should load user projects for DEVELOPER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockDeveloperUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));
      mockIssueService.getAssignedIssues.mockReturnValue(of([]));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProjectService.getUserProjects).toHaveBeenCalledWith(mockDeveloperUser.id);
      expect(component.projects).toEqual(mockProjects);
    });

    it('should handle error when loading projects fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const error = new Error('Failed to load projects');
      
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(throwError(() => error));
      mockIssueService.getAllIssues.mockReturnValue(of([]));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(consoleErrorSpy).toHaveBeenCalledWith('Dashboard - Error loading projects:', error);
      expect(component.projects).toEqual([]);
      consoleErrorSpy.mockRestore();
    });

    it('should not load projects when user role is null', () => {
      component.userRole = null;
      component.loadDashBoard();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
    });
  });

  describe('Data Loading - Issues by Role', () => {
    it('should load all issues for ADMIN role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      mockIssueService.getAllIssues.mockReturnValue(of(mockIssues));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockIssueService.getAllIssues).toHaveBeenCalled();
      expect(component.issues).toEqual(mockIssues);
    });

    it('should load user issues for TESTER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of([]));
      mockIssueService.getIssuesByUser.mockReturnValue(of(mockIssues));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockIssueService.getIssuesByUser).toHaveBeenCalledWith(mockTesterUser.id);
      expect(component.issues).toEqual(mockIssues);
    });

    it('should load assigned issues for DEVELOPER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockDeveloperUser);
      mockProjectService.getUserProjects.mockReturnValue(of([]));
      mockIssueService.getAssignedIssues.mockReturnValue(of(mockIssues));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockIssueService.getAssignedIssues).toHaveBeenCalledWith(mockDeveloperUser.id);
      expect(component.issues).toEqual(mockIssues);
    });

    it('should handle error when loading issues fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const error = new Error('Failed to load issues');
      
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      mockIssueService.getAllIssues.mockReturnValue(throwError(() => error));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(consoleErrorSpy).toHaveBeenCalledWith('Dashboard - Error loading issues:', error);
      expect(component.issues).toEqual([]);
      consoleErrorSpy.mockRestore();
    });

    it('should not load issues when user role is null', () => {
      component.userRole = null;
      component.loadIssues();

      expect(mockIssueService.getAllIssues).not.toHaveBeenCalled();
      expect(mockIssueService.getIssuesByUser).not.toHaveBeenCalled();
      expect(mockIssueService.getAssignedIssues).not.toHaveBeenCalled();
    });
  });

  describe('Display - Projects', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
      mockIssueService.getAllIssues.mockReturnValue(of([]));
    });

    it('should display projects in the grid', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectCards = compiled.querySelectorAll('.project-card');

      expect(projectCards.length).toBe(2);
    });

    it('should display project name', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectNames = compiled.querySelectorAll('.project-name');

      expect(projectNames[0].textContent?.trim()).toBe('Project 1');
      expect(projectNames[1].textContent?.trim()).toBe('Project 2');
    });

    it('should display project description', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const descriptions = compiled.querySelectorAll('.project-description');

      expect(descriptions[0].textContent?.trim()).toBe('Description 1');
      expect(descriptions[1].textContent?.trim()).toBe('Description 2');
    });

    it('should display project status', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const statuses = compiled.querySelectorAll('.project-status');

      expect(statuses[0].textContent?.trim()).toBe('ACTIVE');
      expect(statuses[1].textContent?.trim()).toBe('ARCHIVED');
    });

    it('should apply active class to ACTIVE projects', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const statuses = compiled.querySelectorAll('.project-status');

      expect(statuses[0].classList.contains('active')).toBe(true);
      expect(statuses[1].classList.contains('active')).toBe(false);
    });

    it('should display issue count', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const issueCounts = compiled.querySelectorAll('.project-issues');

      expect(issueCounts[0].textContent?.trim()).toBe('0 issues');
      expect(issueCounts[1].textContent?.trim()).toBe('1 issues');
    });

    it('should display empty state when no projects exist', () => {
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const emptyState = compiled.querySelector('.empty-state');

      expect(emptyState).toBeTruthy();
      expect(emptyState?.textContent?.trim()).toBe('No projects found. Create your first project!');
    });
  });

  describe('Display - Issues', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      mockIssueService.getAllIssues.mockReturnValue(of(mockIssues));
    });

    it('should display issues in the table', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const tableRows = compiled.querySelectorAll('.issues-table tbody tr');

  
      // Should have 2 issue rows (not counting header)
      expect(tableRows.length).toBe(2);
    });

    it('should display issue title', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const rows = compiled.querySelectorAll('.issues-table tbody tr');

      expect(rows[0].querySelector('td:first-child')?.textContent?.trim()).toBe('Issue 1');
      expect(rows[1].querySelector('td:first-child')?.textContent?.trim()).toBe('Issue 2');
    });

    it('should display project name', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const rows = compiled.querySelectorAll('.issues-table tbody tr');

      expect(rows[0].querySelector('td:nth-child(2)')?.textContent?.trim()).toBe('Project 1');
      expect(rows[1].querySelector('td:nth-child(2)')?.textContent?.trim()).toBe('Project 2');
    });

    it('should display N/A when project is missing', () => {
      const issueWithoutProject = {
        ...mockIssues[0],
        project: undefined as any
      };
      mockIssueService.getAllIssues.mockReturnValue(of([issueWithoutProject]));
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectCell = compiled.querySelector('.issues-table tbody tr td:nth-child(2)');

      expect(projectCell?.textContent?.trim()).toBe('N/A');
    });

    it('should display issue status with badge', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const statusBadges = compiled.querySelectorAll('.status-badge');

      expect(statusBadges[0].textContent?.trim()).toBe('OPEN');
      expect(statusBadges[1].textContent?.trim()).toBe('IN_PROGRESS');
    });

    it('should apply correct status badge classes', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const statusBadges = compiled.querySelectorAll('.status-badge');

      expect(statusBadges[0].classList.contains('status-open')).toBe(true);
      expect(statusBadges[1].classList.contains('status-in_progress')).toBe(true);
    });

    it('should display issue priority with badge', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const priorityBadges = compiled.querySelectorAll('.priority-badge');

      expect(priorityBadges[0].textContent?.trim()).toBe('HIGH');
      expect(priorityBadges[1].textContent?.trim()).toBe('MEDIUM');
    });

    it('should apply correct priority badge classes', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const priorityBadges = compiled.querySelectorAll('.priority-badge');

      expect(priorityBadges[0].classList.contains('priority-high')).toBe(true);
      expect(priorityBadges[1].classList.contains('priority-medium')).toBe(true);
    });

    it('should display assigned user username', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const rows = compiled.querySelectorAll('.issues-table tbody tr');

      expect(rows[0].querySelector('td:last-child')?.textContent?.trim()).toBe('developer');
    });

    it('should display Unassigned when no user is assigned', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const rows = compiled.querySelectorAll('.issues-table tbody tr');

      expect(rows[1].querySelector('td:last-child')?.textContent?.trim()).toBe('Unassigned');
    });

    it('should display empty state when no issues exist', () => {
      mockIssueService.getAllIssues.mockReturnValue(of([]));
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const emptyState = compiled.querySelector('.issues-table tbody .empty-state');

      expect(emptyState).toBeTruthy();
      expect(emptyState?.textContent?.trim()).toBe('No issues found.');
      expect(emptyState?.getAttribute('colspan')).toBe('5');
    });
  });

  describe('Display - User Welcome', () => {
    it('should display username in welcome message', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      mockIssueService.getAllIssues.mockReturnValue(of([]));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const welcomeTitle = compiled.querySelector('.dashboard-title');

      expect(welcomeTitle?.textContent?.trim()).toBe('Welcome, admin!');
    });

    it('should display default User when currentUser is null', () => {
      component.currentUser = null;
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const welcomeTitle = compiled.querySelector('.dashboard-title');

      expect(welcomeTitle?.textContent?.trim()).toBe('Welcome, User!');
    });
  });
});
