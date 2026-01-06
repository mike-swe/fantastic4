import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Role } from '../../enum/role';
import { Issues } from '../../interfaces/issues';
import { User } from '../../interfaces/user';
import { Project } from '../../interfaces/project';
import { IssueService } from '../../services/issue-service';
import { ProjectService } from '../../services/project-service';
import { AuthService } from '../../services/auth-service';
import { NewIssueModal } from '../new-issue-modal/new-issue-modal';

@Component({
  selector: 'app-issues',
  imports: [CommonModule, FormsModule, NewIssueModal],
  templateUrl: './issues.html',
  styleUrl: './issues.css',
})
export class IssuesComponent implements OnInit {
  issues: Issues[] = [];
  filteredIssues: Issues[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
  showModal = false;
  readonly Role = Role;

  // Search and filter
  searchQuery = '';
  selectedStatus: string = 'ALL';
  selectedSeverity: string = 'ALL';

  // Status and Severity options
  statusOptions = ['ALL', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
  severityOptions = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private issueService: IssueService,
    private projectService: ProjectService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUserAndIssues();
  }

  private loadUserAndIssues(): void {
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('Issues - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('Issues - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('Issues - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    console.log('Issues - Current User:', this.currentUser);
    console.log('Issues - User Role:', this.userRole);
    
    if (this.currentUser && this.userRole !== null && this.userRole !== undefined) {
      this.loadIssues();
    }
  }

  loadIssues(): void {
    if (this.userRole === null || this.userRole === undefined) {
      console.warn('Issues - No user role set, cannot load issues');
      return;
    }

    if (this.userRole === Role.ADMIN) {
      this.issueService.getAllIssues().subscribe({
        next: (issues) => {
          this.issues = issues;
          this.applyFilters();
          console.log('Issues - Loaded issues:', issues.length);
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Issues - Error loading issues:', error);
        }
      });
    } else if (this.userRole === Role.TESTER || this.userRole === Role.DEVELOPER) {
      if (this.currentUser?.id) {
        this.issueService.getIssuesByUser(this.currentUser.id).subscribe({
          next: (issues) => {
            this.issues = issues;
            this.applyFilters();
            console.log('Issues - Loaded user issues:', issues.length);
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Issues - Error loading user issues:', error);
          }
        });
      } else {
        console.warn('Issues - No user ID available');
      }
    }
  }


  applyFilters(): void {
    this.filteredIssues = this.issues.filter(issue => {
      // Search filter
      const matchesSearch = !this.searchQuery || 
        issue.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        this.formatIssueId(issue.id).toLowerCase().includes(this.searchQuery.toLowerCase());

      // Status filter
      const matchesStatus = this.selectedStatus === 'ALL' || 
        issue.status === this.selectedStatus;

      // Severity filter
      const matchesSeverity = this.selectedSeverity === 'ALL' || 
        issue.severity === this.selectedSeverity;

      return matchesSearch && matchesStatus && matchesSeverity;
    });
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusChange(): void {
    this.applyFilters();
  }

  onSeverityChange(): void {
    this.applyFilters();
  }

  createNewIssue(): void {
    this.showModal = true;
  }

  onIssueCreated(issue: Issues): void {
    this.showModal = false;
    this.loadIssues();
  }

  onModalClose(): void {
    this.showModal = false;
  }

  // Helper functions
  formatIssueId(id: string): string {
    // Format UUID as ISS-XXXX (first 8 chars)
    if (!id) return 'ISS-0000';
    const shortId = id.replace(/-/g, '').substring(0, 8).toUpperCase();
    return `ISS-${shortId}`;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toISOString().split('T')[0]; // YYYY-MM-DD format
    } catch {
      return 'N/A';
    }
  }

  getUserInitials(user: User | null | undefined): string {
    if (!user || !user.username) return '??';
    const parts = user.username.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return user.username.substring(0, 2).toUpperCase();
  }

  truncateDescription(description: string, maxLength: number = 150): string {
    if (!description) return '';
    if (description.length <= maxLength) return description;
    return description.substring(0, maxLength) + '...';
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'OPEN': return '#f97316'; // orange
      case 'IN_PROGRESS': return '#3b82f6'; // blue
      case 'RESOLVED': return '#10b981'; // green
      case 'CLOSED': return '#6b7280'; // grey
      default: return '#6b7280';
    }
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'LOW': return '#10b981'; // green
      case 'MEDIUM': return '#f97316'; // orange
      case 'HIGH': return '#ef4444'; // red
      case 'CRITICAL': return '#dc2626'; // red
      default: return '#6b7280';
    }
  }

  getPriorityColor(priority: string): string {
    return this.getSeverityColor(priority); // Same color scheme as severity
  }
}

