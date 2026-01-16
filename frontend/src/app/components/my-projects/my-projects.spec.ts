import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { MyProjects } from './my-projects';
import { ProjectService } from '../../services/project-service';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { User } from '../../interfaces/user';

describe('MyProjects', () => {
  let component: MyProjects;
  let fixture: ComponentFixture<MyProjects>;
  let mockAuthService: {
    getToken: ReturnType<typeof vi.fn>;
    isAuthenticated: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
  };
  let mockProjectService: {
    getUserProjects: ReturnType<typeof vi.fn>;
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
      issues: []
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
      getUserProjects: vi.fn()
    };

    mockChangeDetectorRef = {
      detectChanges: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [MyProjects],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: ChangeDetectorRef, useValue: mockChangeDetectorRef }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyProjects);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Authentication & Authorization', () => {
    it('should not load projects when no token is present', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      mockAuthService.getToken.mockReturnValue(null);
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(consoleWarnSpy).toHaveBeenCalledWith('MyProjects - No token found');
      consoleWarnSpy.mockRestore();
    });

    it('should not load projects when user is not authenticated', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(consoleWarnSpy).toHaveBeenCalledWith('MyProjects - User not authenticated');
      consoleWarnSpy.mockRestore();
    });

    it('should not load projects when current user is null', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(null);

      fixture.detectChanges();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(consoleWarnSpy).toHaveBeenCalledWith('MyProjects - Could not get current user');
      consoleWarnSpy.mockRestore();
    });

    it('should not load projects when user is ADMIN', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);

      fixture.detectChanges();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        'MyProjects - Only TESTER or DEVELOPER can access this page'
      );
      consoleWarnSpy.mockRestore();
    });
  });

  describe('Project Loading', () => {
    it('should load user projects for TESTER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProjectService.getUserProjects).toHaveBeenCalledWith(mockTesterUser.id);
      expect(component.projects).toEqual(mockProjects);
      expect(component.userRole).toBe(Role.TESTER);
    });

    it('should load user projects for DEVELOPER role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockDeveloperUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProjectService.getUserProjects).toHaveBeenCalledWith(mockDeveloperUser.id);
      expect(component.projects).toEqual(mockProjects);
      expect(component.userRole).toBe(Role.DEVELOPER);
    });

    it('should handle error when loading projects fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const error = new Error('Failed to load projects');

      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(throwError(() => error));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'MyProjects - Error loading user projects:',
        error
      );
      expect(component.projects).toEqual([]);
      consoleErrorSpy.mockRestore();
    });

    it('should not load projects when user ID is not available', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      const userWithoutId = { ...mockTesterUser, id: undefined as any };
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(userWithoutId);
      component.userRole = Role.TESTER;
      component.currentUser = userWithoutId;

      component.loadProjects();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(consoleWarnSpy).toHaveBeenCalledWith('MyProjects - No user ID available');
      consoleWarnSpy.mockRestore();
    });
  });

  describe('Project List Rendering', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));
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

    it('should display empty state when no projects exist', () => {
      mockProjectService.getUserProjects.mockReturnValue(of([]));
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const emptyState = compiled.querySelector('.empty-state');

      expect(emptyState).toBeTruthy();
      expect(emptyState?.textContent?.trim()).toContain('No projects assigned');
      expect(emptyState?.textContent?.trim()).toContain('Contact an administrator');
    });

    it('should render correct number of project cards', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectCards = compiled.querySelectorAll('.project-card');

      expect(projectCards.length).toBe(mockProjects.length);
    });
  });

  describe('Read-Only Features', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));
    });

    it('should not show "New Project" button in header', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const newProjectButton = compiled.querySelector('.btn-new-project');

      expect(newProjectButton).toBeFalsy();
    });

    it('should not show Edit button on project cards', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const editButtons = compiled.querySelectorAll('.btn-edit');

      expect(editButtons.length).toBe(0);
    });

    it('should not show Delete button on project cards', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const deleteButtons = compiled.querySelectorAll('.btn-delete');

      expect(deleteButtons.length).toBe(0);
    });

    it('should not show project actions section', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectActions = compiled.querySelectorAll('.project-actions');

      expect(projectActions.length).toBe(0);
    });

    it('should display read-only project information', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const projectCards = compiled.querySelectorAll('.project-card');

      projectCards.forEach((card) => {
        const name = card.querySelector('.project-name');
        const description = card.querySelector('.project-description');
        expect(name).toBeTruthy();
        expect(description).toBeTruthy();
      });
    });
  });
});
