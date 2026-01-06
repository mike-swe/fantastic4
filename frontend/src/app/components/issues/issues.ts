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
import { IssueDetailModal } from '../issue-detail-modal/issue-detail-modal';

@Component({
  selector: 'app-issues',
  imports: [CommonModule, FormsModule, NewIssueModal, IssueDetailModal],
  templateUrl: './issues.html',
  styleUrl: './issues.css',
})
export class IssuesComponent implements OnInit {
  issues: Issues[] = [];
  filteredIssues: Issues[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
  showModal = false;
  showDetailModal = false;
  selectedIssue: Issues | null = null;
  readonly Role = Role;

  searchQuery = '';
  selectedStatus: string = 'ALL';
  selectedSeverity: string = 'ALL';
  selectedPriority: string = 'ALL';
  selectedAssignedTo: string = 'ALL';
  selectedDateRange: string = 'ALL';
  customFromDate: string = '';
  customToDate: string = '';

  statusOptions = ['ALL', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
  severityOptions = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  priorityOptions = ['ALL', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  dateRangeOptions = ['ALL', 'TODAY', 'THIS_WEEK', 'THIS_MONTH', 'LAST_30_DAYS', 'CUSTOM'];
  assignedUsers: User[] = [];

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
          this.extractAssignedUsers();
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
            this.extractAssignedUsers();
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
      const matchesSearch = !this.searchQuery || 
        issue.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        this.formatIssueId(issue.id).toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        issue.project?.name?.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus = this.selectedStatus === 'ALL' || 
        issue.status === this.selectedStatus;

      const matchesSeverity = this.selectedSeverity === 'ALL' || 
        issue.severity === this.selectedSeverity;

      const matchesPriority = this.selectedPriority === 'ALL' || 
        issue.priority === this.selectedPriority;

      const matchesAssignedTo = this.selectedAssignedTo === 'ALL' || 
        (this.selectedAssignedTo === 'UNASSIGNED' && !issue.assignedTo) ||
        (issue.assignedTo?.id === this.selectedAssignedTo);

      const matchesDateRange = this.matchesDateRange(issue);

      return matchesSearch && matchesStatus && matchesSeverity && matchesPriority && matchesAssignedTo && matchesDateRange;
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

  onPriorityChange(): void {
    this.applyFilters();
  }

  onAssignedToChange(): void {
    this.applyFilters();
  }

  onDateRangeChange(): void {
    this.applyFilters();
  }

  matchesDateRange(issue: Issues): boolean {
    if (this.selectedDateRange === 'ALL') {
      return true;
    }

    const issueDate = issue.updatedAt ? new Date(issue.updatedAt) : new Date(issue.createdAt);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    switch (this.selectedDateRange) {
      case 'TODAY':
        return issueDate >= today;
      case 'THIS_WEEK':
        const weekStart = new Date(today);
        weekStart.setDate(today.getDate() - today.getDay());
        return issueDate >= weekStart;
      case 'THIS_MONTH':
        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
        return issueDate >= monthStart;
      case 'LAST_30_DAYS':
        const thirtyDaysAgo = new Date(today);
        thirtyDaysAgo.setDate(today.getDate() - 30);
        return issueDate >= thirtyDaysAgo;
      case 'CUSTOM':
        if (!this.customFromDate || !this.customToDate) {
          return true;
        }
        const fromDate = new Date(this.customFromDate);
        const toDate = new Date(this.customToDate);
        toDate.setHours(23, 59, 59, 999);
        return issueDate >= fromDate && issueDate <= toDate;
      default:
        return true;
    }
  }

  extractAssignedUsers(): void {
    const userMap = new Map<string, User>();
    this.issues.forEach(issue => {
      if (issue.assignedTo) {
        userMap.set(issue.assignedTo.id, issue.assignedTo);
      }
    });
    this.assignedUsers = Array.from(userMap.values());
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

  openIssueDetail(issue: Issues): void {
    this.selectedIssue = issue;
    this.showDetailModal = true;
  }

  onDetailModalClose(): void {
    this.showDetailModal = false;
    this.selectedIssue = null;
  }

  formatIssueId(id: string): string {
    if (!id) return 'ISS-0000';
    const shortId = id.replace(/-/g, '').substring(0, 8).toUpperCase();
    return `ISS-${shortId}`;
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toISOString().split('T')[0];
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
      case 'OPEN': return '#f97316';
      case 'IN_PROGRESS': return '#3b82f6';
      case 'RESOLVED': return '#10b981';
      case 'CLOSED': return '#6b7280';
      default: return '#6b7280';
    }
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'LOW': return '#10b981';
      case 'MEDIUM': return '#f97316';
      case 'HIGH': return '#ef4444';
      case 'CRITICAL': return '#dc2626';
      default: return '#6b7280';
    }
  }

  getPriorityColor(priority: string): string {
    return this.getSeverityColor(priority);
  }
}

