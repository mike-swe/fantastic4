import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Role } from '../../enum/role';
import { AuditLog } from '../../interfaces/audit-log';
import { User } from '../../interfaces/user';
import { AuditService } from '../../services/audit-service';
import { AuthService } from '../../services/auth-service';
import { UserService } from '../../services/user-service';

@Component({
  selector: 'app-audit-logs',
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css',
})
export class AuditLogs implements OnInit {
  auditLogs: AuditLog[] = [];
  filteredLogs: AuditLog[] = [];
  currentUser: User | null = null;
  userRole: Role | null = null;
  users: User[] = [];
  readonly Role = Role;

  selectedEntityType: string = 'ALL';
  selectedActorId: string = 'ALL';
  isLoading = false;
  errorMessage: string | null = null;

  entityTypes: string[] = ['ALL', 'ISSUE', 'PROJECT', 'PROJECT_ASSIGNMENT'];
  actorOptions: Array<{ id: string; username: string }> = [];

  constructor(
    private auditService: AuditService,
    private authService: AuthService,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUserAndData();
  }

  private loadUserAndData(): void {
    const token = this.authService.getToken();
    
    if (!token) {
      console.warn('AuditLogs - No token found');
      return;
    }

    if (!this.authService.isAuthenticated()) {
      console.warn('AuditLogs - User not authenticated');
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      console.warn('AuditLogs - Could not get current user');
      return;
    }

    this.userRole = this.currentUser.role ?? null;
    
    if (this.userRole === Role.ADMIN) {
      this.loadUsers();
      this.loadAuditLogs();
    } else {
      this.errorMessage = 'Only Admin users can access audit logs';
    }
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.actorOptions = [
          { id: 'ALL', username: 'All Users' },
          ...users.map(u => ({ id: u.id, username: u.username }))
        ];
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('AuditLogs - Error loading users:', error);
      }
    });
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.auditService.getAllAuditLogs().subscribe({
      next: (logs) => {
        this.auditLogs = logs;
        this.applyFilters();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('AuditLogs - Error loading audit logs:', error);
        this.errorMessage = 'Failed to load audit logs. Please try again.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.auditLogs];

    if (this.selectedEntityType !== 'ALL') {
      filtered = filtered.filter(log => log.entityType === this.selectedEntityType);
    }

    if (this.selectedActorId !== 'ALL') {
      filtered = filtered.filter(log => log.actorUserId === this.selectedActorId);
    }

    this.filteredLogs = filtered;
  }

  onEntityTypeChange(): void {
    this.applyFilters();
  }

  onActorChange(): void {
    this.applyFilters();
  }

  formatTimestamp(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  getActorUsername(actorId: string): string {
    const user = this.users.find(u => u.id === actorId);
    return user ? user.username : 'Unknown';
  }

  formatAction(action: string): string {
    return action.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  formatEntityType(entityType: string): string {
    return entityType.replace(/_/g, ' ').toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}

