import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { User } from '../../interfaces/user';
import { ProjectService } from '../../services/project-service';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-my-projects',
  imports: [],
  templateUrl: './my-projects.html',
  styleUrl: './my-projects.css',
})
export class MyProjects implements OnInit {
  projects: Project[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
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
      console.warn('MyProjects - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('MyProjects - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('MyProjects - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    console.log('MyProjects - Current User:', this.currentUser);
    console.log('MyProjects - User Role:', this.userRole);
    
    if ((this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER) && this.currentUser.id) {
      this.loadProjects();
    } else {
      console.warn('MyProjects - Only TESTER or DEVELOPER can access this page');
    }
  }

  loadProjects(): void {
    if (!this.currentUser?.id) {
      console.warn('MyProjects - No user ID available');
      return;
    }

    this.projectService.getUserProjects(this.currentUser.id).subscribe({
      next: (projects) => {
        this.projects = projects;
        console.log('MyProjects - Loaded user projects:', projects.length);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('MyProjects - Error loading user projects:', error);
      }
    });
  }
}

