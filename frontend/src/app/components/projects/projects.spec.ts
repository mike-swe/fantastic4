import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChangeDetectorRef } from '@angular/core';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Projects } from './projects';
import { ProjectService } from '../../services/project-service';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { User } from '../../interfaces/user';
import { NewProjectModal } from '../new-project-modal/new-project-modal';

describe('Projects', () => {
  let component: Projects;
  let fixture: ComponentFixture<Projects>;
  let mockAuthService: {
    getToken: ReturnType<typeof vi.fn>;
    isAuthenticated: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
  };
  let mockProjectService: {
    getAllProjects: ReturnType<typeof vi.fn>;
    getUserProjects: ReturnType<typeof vi.fn>;
    deleteProject: ReturnType<typeof vi.fn>;
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
      getAllProjects: vi.fn(),
      getUserProjects: vi.fn(),
      deleteProject: vi.fn()
    };

    mockChangeDetectorRef = {
      detectChanges: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Projects, NewProjectModal],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: ChangeDetectorRef, useValue: mockChangeDetectorRef }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Projects);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Testing: Authentication & Authorization 
  describe('Authentication & Authorization', () => {
    it('should not load projects when no token is present', () => {
      mockAuthService.getToken.mockReturnValue(null);
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
    });

    it('should not load projects when user is not authenticated', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(false);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
    });

    it('should not load projects when current user is null', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(null);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(component.projects).toEqual([]);
    });

    it('should not load projects when user has no role', () => {
      const userWithoutRole = { ...mockAdminUser, role: undefined as any };
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(userWithoutRole);

      fixture.detectChanges();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
    });
  });

  // Testing: loadProjects() method 
  describe('Project Loading by Role', () => {
    it('should load all projects for ADMIN role', async () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockProjectService.getAllProjects).toHaveBeenCalled();
      expect(component.projects).toEqual(mockProjects);
      expect(component.userRole).toBe(Role.ADMIN);
    });

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
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(throwError(() => error));

      fixture.detectChanges();
      await fixture.whenStable();

      expect(consoleErrorSpy).toHaveBeenCalledWith('Projects - Error loading projects:', error);
      expect(component.projects).toEqual([]);
      consoleErrorSpy.mockRestore();
    });

    it('should not load projects when user role is null', () => {
      component.userRole = null;
      component.loadProjects();

      expect(mockProjectService.getAllProjects).not.toHaveBeenCalled();
      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
    });

    it('should not load projects when user ID is not available for TESTER', () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      const userWithoutId = { ...mockTesterUser, id: undefined as any };
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(userWithoutId);
      component.userRole = Role.TESTER;
      component.currentUser = userWithoutId;

      component.loadProjects();

      expect(mockProjectService.getUserProjects).not.toHaveBeenCalled();
      expect(consoleWarnSpy).toHaveBeenCalledWith('Projects - No user ID available');
      consoleWarnSpy.mockRestore();
    });
  });

  // Testing: Template rendering and display logic
  describe('Project List Rendering', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
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
      mockProjectService.getAllProjects.mockReturnValue(of([]));
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const emptyState = compiled.querySelector('.empty-state');

      expect(emptyState).toBeTruthy();
      expect(emptyState?.textContent?.trim()).toContain('No projects found');
    });
  });

  // Testing: UI visibility based on user role (template conditional rendering)
  describe('Admin-Only Features - UI Visibility', () => {
    it('should show "New Project" button only for ADMIN', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const newProjectButton = compiled.querySelector('.btn-new-project');

      expect(newProjectButton).toBeTruthy();
      expect(component.userRole).toBe(Role.ADMIN);
    });

    it('should hide "New Project" button for TESTER', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const newProjectButton = compiled.querySelector('.btn-new-project');

      expect(newProjectButton).toBeFalsy();
    });

    it('should hide "New Project" button for DEVELOPER', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockDeveloperUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const newProjectButton = compiled.querySelector('.btn-new-project');

      expect(newProjectButton).toBeFalsy();
    });

    it('should show Edit and Delete buttons only for ADMIN', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const editButtons = compiled.querySelectorAll('.btn-edit');
      const deleteButtons = compiled.querySelectorAll('.btn-delete');

      expect(editButtons.length).toBe(2);
      expect(deleteButtons.length).toBe(2);
    });

    it('should hide Edit and Delete buttons for TESTER', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockTesterUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const editButtons = compiled.querySelectorAll('.btn-edit');
      const deleteButtons = compiled.querySelectorAll('.btn-delete');

      expect(editButtons.length).toBe(0);
      expect(deleteButtons.length).toBe(0);
    });

    it('should hide Edit and Delete buttons for DEVELOPER', () => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockDeveloperUser);
      mockProjectService.getUserProjects.mockReturnValue(of(mockProjects));

      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const editButtons = compiled.querySelectorAll('.btn-edit');
      const deleteButtons = compiled.querySelectorAll('.btn-delete');

      expect(editButtons.length).toBe(0);
      expect(deleteButtons.length).toBe(0);
    });
  });

  // Testing: createNewProject() and onProjectCreated() methods
  describe('Create Project Operation', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
    });

    it('should open modal with editingProject set to null when creating new project', () => {
      fixture.detectChanges();
      component.createNewProject();

      expect(component.editingProject).toBeNull();
      expect(component.showModal).toBe(true);
    });

    it('should show modal when showModal is true', () => {
      component.showModal = true;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const modal = compiled.querySelector('app-new-project-modal');

      expect(modal).toBeTruthy();
    });

    it('should close modal and reload projects when project is created', async () => {
      fixture.detectChanges();
      component.showModal = true;
      component.editingProject = null;
      mockProjectService.getAllProjects.mockReturnValue(of([...mockProjects, mockProjects[0]]));

      component.onProjectCreated(mockProjects[0]);
      await fixture.whenStable();

      expect(component.showModal).toBe(false);
      expect(component.editingProject).toBeNull();
      expect(mockProjectService.getAllProjects).toHaveBeenCalled();
    });
  });

  // Testing: editProject() and onProjectUpdated() methods
  describe('Update Project Operation', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
    });

    it('should set editingProject and open modal when editing project', () => {
      fixture.detectChanges();
      const projectToEdit = mockProjects[0];

      component.editProject(projectToEdit);

      expect(component.editingProject).toEqual(projectToEdit);
      expect(component.showModal).toBe(true);
    });

    it('should pass correct project data to modal in edit mode', () => {
      const projectToEdit = mockProjects[0];
      component.editProject(projectToEdit);
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const modal = compiled.querySelector('app-new-project-modal');

      expect(modal).toBeTruthy();
      expect(component.editingProject).toEqual(projectToEdit);
      expect(component.showModal).toBe(true);
    });

    it('should close modal and reload projects when project is updated', async () => {
      fixture.detectChanges();
      component.showModal = true;
      component.editingProject = mockProjects[0];
      const updatedProject = { ...mockProjects[0], name: 'Updated Project' };
      mockProjectService.getAllProjects.mockReturnValue(of([updatedProject, mockProjects[1]]));

      component.onProjectUpdated(updatedProject);
      await fixture.whenStable();

      expect(component.showModal).toBe(false);
      expect(component.editingProject).toBeNull();
      expect(mockProjectService.getAllProjects).toHaveBeenCalled();
    });
  });

  // Testing: deleteProject() method
  describe('Delete Project Operation', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
    });

    it('should show confirmation dialog when deleting project', () => {
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      mockProjectService.deleteProject.mockReturnValue(of(void 0));
      mockProjectService.getAllProjects.mockReturnValue(of([mockProjects[1]]));

      fixture.detectChanges();
      component.deleteProject(mockProjects[0]);

      expect(confirmSpy).toHaveBeenCalledWith(
        expect.stringContaining('Are you sure you want to delete')
      );
      confirmSpy.mockRestore();
    });

    it('should not delete project when confirmation is cancelled', () => {
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

      fixture.detectChanges();
      component.deleteProject(mockProjects[0]);

      expect(mockProjectService.deleteProject).not.toHaveBeenCalled();
      confirmSpy.mockRestore();
    });

    it('should call deleteProject service method when confirmed', async () => {
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      mockProjectService.deleteProject.mockReturnValue(of(void 0));
      mockProjectService.getAllProjects.mockReturnValue(of([mockProjects[1]]));

      fixture.detectChanges();
      component.deleteProject(mockProjects[0]);
      await fixture.whenStable();

      expect(mockProjectService.deleteProject).toHaveBeenCalledWith(mockProjects[0].id);
      confirmSpy.mockRestore();
    });

    it('should reload projects after successful deletion', async () => {
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      mockProjectService.deleteProject.mockReturnValue(of(void 0));
      mockProjectService.getAllProjects.mockReturnValue(of([mockProjects[1]]));

      fixture.detectChanges();
      component.deleteProject(mockProjects[0]);
      await fixture.whenStable();

      expect(mockProjectService.getAllProjects).toHaveBeenCalled();
      confirmSpy.mockRestore();
    });

    it('should handle error when deletion fails', async () => {
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const error = new Error('Failed to delete project');
      mockProjectService.deleteProject.mockReturnValue(throwError(() => error));

      fixture.detectChanges();
      component.deleteProject(mockProjects[0]);
      await fixture.whenStable();

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error deleting project:', error);
      expect(alertSpy).toHaveBeenCalledWith('Failed to delete project. Please try again.');
      confirmSpy.mockRestore();
      alertSpy.mockRestore();
      consoleErrorSpy.mockRestore();
    });
  });

  // Testing: onModalClose() method and modal state management
  describe('Modal Management', () => {
    beforeEach(() => {
      mockAuthService.getToken.mockReturnValue('some-token');
      mockAuthService.isAuthenticated.mockReturnValue(true);
      mockAuthService.getCurrentUser.mockReturnValue(mockAdminUser);
      mockProjectService.getAllProjects.mockReturnValue(of(mockProjects));
    });

    it('should close modal and clear editingProject when onModalClose is called', () => {
      fixture.detectChanges();
      component.showModal = true;
      component.editingProject = mockProjects[0];

      component.onModalClose();

      expect(component.showModal).toBe(false);
      expect(component.editingProject).toBeNull();
    });

    it('should pass correct props to modal component', () => {
      component.showModal = true;
      component.editingProject = mockProjects[0];
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      const modal = compiled.querySelector('app-new-project-modal');

      expect(modal).toBeTruthy();
      expect(component.editingProject).toEqual(mockProjects[0]);
    });

    it('should pass isEditMode as true when editingProject is not null', () => {
      component.showModal = true;
      component.editingProject = mockProjects[0];
      fixture.detectChanges();

      expect(component.editingProject).not.toBeNull();
    });

    it('should pass isEditMode as false when editingProject is null', () => {
      component.showModal = true;
      component.editingProject = null;
      fixture.detectChanges();

      expect(component.editingProject).toBeNull();
    });
  });
});
