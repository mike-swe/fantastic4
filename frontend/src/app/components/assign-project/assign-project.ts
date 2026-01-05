import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { User } from '../../interfaces/user';
import { ProjectService } from '../../services/project-service';
import { UserService } from '../../services/user-service';
import { AuthService } from '../../services/auth-service';

interface ProjectWithUsers extends Project {
  assignedUsers: User[];
  selectedUserId: string;
  isAssigning: boolean;
}

@Component({
  selector: 'app-assign-project',
  imports: [CommonModule, FormsModule],
  templateUrl: './assign-project.html',
  styleUrl: './assign-project.css',
})
export class AssignProject implements OnInit {
  projects: ProjectWithUsers[] = [];
  allUsers: User[] = [];
  availableUsers: User[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
  readonly Role = Role;

  successMessage = '';
  errorMessage = '';
  usersLoaded = false;

  constructor(
    private projectService: ProjectService,
    private userService: UserService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUserAndData();
  }

  private loadUserAndData(): void {
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('AssignProject - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('AssignProject - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('AssignProject - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    console.log('AssignProject - Current User:', this.currentUser);
    console.log('AssignProject - User Role:', this.userRole);
    
    if (this.userRole === Role.ADMIN) {
      this.loadProjects();
      this.loadUsers();
    } else {
      console.warn('AssignProject - Only Admin can access this page');
    }
  }

  loadProjects(): void {
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects.map(project => ({
          ...project,
          assignedUsers: [],
          selectedUserId: '',
          isAssigning: false
        }));
        
        this.projects.forEach(project => {
          this.loadProjectUsers(project);
        });
        
        console.log('AssignProject - Loaded projects:', projects.length);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('AssignProject - Error loading projects:', error);
        this.showError('Failed to load projects');
      }
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.availableUsers = users.filter(user => 
          user.role === Role.TESTER || user.role === Role.DEVELOPER
        );
        this.usersLoaded = true;
        console.log('AssignProject - Loaded users:', users.length);
        console.log('AssignProject - Available users (TESTER/DEVELOPER):', this.availableUsers.length);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('AssignProject - Error loading users:', error);
        this.showError('Failed to load users');
        this.usersLoaded = true;
      }
    });
  }

  loadProjectUsers(project: ProjectWithUsers): void {
    this.userService.getProjectUsers(project.id).subscribe({
      next: (users) => {
        project.assignedUsers = users;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error(`AssignProject - Error loading users for project ${project.id}:`, error);
        project.assignedUsers = [];
        this.cdr.detectChanges();
      }
    });
  }

  assignUser(project: ProjectWithUsers): void {
    if (!project.selectedUserId) {
      this.showError('Please select a user to assign');
      return;
    }

    const isAlreadyAssigned = project.assignedUsers.some(
      user => user.id === project.selectedUserId
    );

    if (isAlreadyAssigned) {
      this.showError('This user is already assigned to this project');
      return;
    }

    project.isAssigning = true;
    this.clearMessages();

    this.userService.assignUserToProject(project.id, project.selectedUserId).subscribe({
      next: () => {
        project.isAssigning = false;
        project.selectedUserId = '';
        this.loadProjectUsers(project);
        this.showSuccess('User assigned successfully');
        this.cdr.detectChanges();
      },
      error: (error) => {
        project.isAssigning = false;
        console.error('AssignProject - Error assigning user:', error);
        if (error.error?.message) {
          this.showError(error.error.message);
        } else if (error.status === 403) {
          this.showError('Only Admin users can assign users to projects');
        } else {
          this.showError('Failed to assign user. Please try again.');
        }
        this.cdr.detectChanges();
      }
    });
  }

  getAvailableUsersForProject(project: ProjectWithUsers): User[] {
    return this.availableUsers.filter(user => 
      !project.assignedUsers.some(assigned => assigned.id === user.id)
    );
  }

  getUserRoleBadgeClass(role: Role): string {
    switch (role) {
      case Role.TESTER:
        return 'badge-tester';
      case Role.DEVELOPER:
        return 'badge-developer';
      case Role.ADMIN:
        return 'badge-admin';
      default:
        return 'badge-default';
    }
  }

  getUserInitials(user: User): string {
    if (!user || !user.username) return '??';
    const parts = user.username.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return user.username.substring(0, 2).toUpperCase();
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => {
      this.successMessage = '';
    }, 5000);
  }

  private showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => {
      this.errorMessage = '';
    }, 5000);
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}

