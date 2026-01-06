import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Role } from '../../enum/role';
import { Project } from '../../interfaces/project';
import { Issues} from '../../interfaces/issues';
import { User } from '../../interfaces/user';
import { ProjectService } from '../../services/project-service';
import { IssueService } from '../../services/issue-service';
import { AuthService } from '../../services/auth-service';


@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  currentUser: User | null = null;
  userRole: Role | null = null;
  projects: Project[] = [];
  issues: Issues[] = [];

  constructor(
    private projectService: ProjectService,
    private issueService: IssueService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    this.loadUserAndData();
  }

  private loadUserAndData(): void {
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('Dashboard - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('Dashboard - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('Dashboard - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    console.log('Dashboard - Current User:', this.currentUser);
    console.log('Dashboard - User Role:', this.userRole);
    
    if (this.currentUser && this.userRole !== null && this.userRole !== undefined) {
      this.loadDashBoard();
      this.loadIssues();
    }
  }

  loadDashBoard(): void {
    if (this.userRole === null || this.userRole === undefined) {
      console.warn('Dashboard - No user role set, cannot load projects');
      return;
    }

    if (this.userRole === Role.ADMIN) {
      this.projectService.getAllProjects().subscribe({
        next: (projects) => {
          this.projects = projects;
          console.log('Dashboard - Loaded projects:', projects.length);
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Dashboard - Error loading projects:', error);
        }
      });
    } else if (this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER) {
      if (this.currentUser?.id) {
        this.projectService.getUserProjects(this.currentUser.id).subscribe({
          next: (projects) => {
            this.projects = projects;
            console.log('Dashboard - Loaded user projects:', projects.length);
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Dashboard - Error loading user projects:', error);
          }
        });
      }
    }
  }

  loadIssues(): void {
    if (this.userRole === null || this.userRole === undefined) {
      console.warn('Dashboard - No user role set, cannot load issues');
      return;
    }

    if (this.userRole === Role.ADMIN) {
      this.issueService.getAllIssues().subscribe({
        next: (issues) => {
          this.issues = issues;
          console.log('Dashboard - Loaded issues:', issues.length);
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Dashboard - Error loading issues:', error);
        }
      });
    } else if (this.userRole === Role.TESTER) {
      if (this.currentUser?.id) {
        this.issueService.getIssuesByUser(this.currentUser.id).subscribe({
          next: (issues) => {
            this.issues = issues;
            console.log('Dashboard - Loaded user issues:', issues.length);
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Dashboard - Error loading user issues:', error);
          }
        });
      }
    } else if (this.userRole === Role.DEVELOPER) {
      if (this.currentUser?.id) {
        this.issueService.getAssignedIssues(this.currentUser.id).subscribe({
          next: (issues) => {
            this.issues = issues;
            console.log('Dashboard - Loaded assigned issues:', issues.length);
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Dashboard - Error loading assigned issues:', error);
          }
        });
      }
    }
  }
}
