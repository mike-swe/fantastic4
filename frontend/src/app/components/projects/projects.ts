import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { User } from '../../interfaces/user';
import { ProjectService } from '../../services/project-service';
import { AuthService } from '../../services/auth-service';
import { NewProjectModal } from '../new-project-modal/new-project-modal';

@Component({
  selector: 'app-projects',
  imports: [NewProjectModal],
  templateUrl: './projects.html',
  styleUrl: './projects.css',
})
export class Projects implements OnInit {
  projects: Project[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
  showModal = false;
  editingProject: Project | null = null;
  readonly Role = Role;

  constructor(
    private projectService: ProjectService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUserAndProjects();
  }

  private loadUserAndProjects(): void {
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('Projects - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('Projects - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('Projects - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    console.log('Projects - Current User:', this.currentUser);
    console.log('Projects - User Role:', this.userRole);
    
    if (this.currentUser && this.userRole !== null && this.userRole !== undefined) {
      this.loadProjects();
    }
  }

  loadProjects(): void {
    if (this.userRole === null || this.userRole === undefined) {
      console.warn('Projects - No user role set, cannot load projects');
      return;
    }

    if (this.userRole === Role.ADMIN) {
      this.projectService.getAllProjects().subscribe({
        next: (projects) => {
          this.projects = projects;
          console.log('Projects - Loaded projects:', projects.length);
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Projects - Error loading projects:', error);
        }
      });
    } else if (this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER) {
      if (this.currentUser?.id) {
        this.projectService.getUserProjects(this.currentUser.id).subscribe({
          next: (projects) => {
            this.projects = projects;
            console.log('Projects - Loaded user projects:', projects.length);
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Projects - Error loading user projects:', error);
          }
        });
      } else {
        console.warn('Projects - No user ID available');
      }
    }
  }

  createNewProject(): void {
    this.editingProject = null;
    this.showModal = true;
  }

  editProject(project: Project): void {
    this.editingProject = project;
    this.showModal = true;
  }

  deleteProject(project: Project): void {
    if (!confirm(`Are you sure you want to delete "${project.name}"? This action cannot be undone and will delete all associated issues and assignments.`)) {
      return;
    }

    this.projectService.deleteProject(project.id).subscribe({
      next: () => {
        this.loadProjects();
      },
      error: (error) => {
        console.error('Error deleting project:', error);
        alert('Failed to delete project. Please try again.');
      }
    });
  }

  onProjectCreated(project: Project): void {
    this.showModal = false;
    this.editingProject = null;
    this.loadProjects(); 
  }

  onProjectUpdated(project: Project): void {
    this.showModal = false;
    this.editingProject = null;
    this.loadProjects(); 
  }

  onModalClose(): void {
    this.showModal = false;
    this.editingProject = null;
  }
}
