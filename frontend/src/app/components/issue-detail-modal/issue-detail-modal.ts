import { Component, Input, OnInit, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Issues } from '../../interfaces/issues';
import { IssueHistory } from '../../interfaces/issue-history';
import { IssueService } from '../../services/issue-service';

@Component({
  selector: 'app-issue-detail-modal',
  imports: [CommonModule],
  templateUrl: './issue-detail-modal.html',
  styleUrl: './issue-detail-modal.css',
})
export class IssueDetailModal implements OnInit {
  @Input() issue: Issues | null = null;
  @Output() close = new EventEmitter<void>();

  history: IssueHistory[] = [];
  isLoadingHistory = signal(false);
  errorMessage = signal('');

  constructor(private issueService: IssueService) {}

  ngOnInit(): void {
    if (this.issue) {
      this.loadHistory();
    }
  }

  loadHistory(): void {
    if (!this.issue?.id) {
      return;
    }

    this.isLoadingHistory.set(true);
    this.errorMessage.set('');

    this.issueService.getIssueHistory(this.issue.id).subscribe({
      next: (history) => {
        this.history = history;
        this.isLoadingHistory.set(false);
      },
      error: (error) => {
        console.error('Error loading issue history:', error);
        this.errorMessage.set('Failed to load issue history');
        this.isLoadingHistory.set(false);
      }
    });
  }

  onCancel(): void {
    this.close.emit();
  }

  onBackdropClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return 'N/A';
    }
  }

  formatChangeDescription(historyItem: IssueHistory): string {
    if (historyItem.changeType === 'CREATED') {
      return historyItem.newValue || 'Issue created';
    }

    if (historyItem.changeType === 'STATUS_CHANGE') {
      return `Status changed from ${this.formatFieldValue(historyItem.oldValue)} to ${this.formatFieldValue(historyItem.newValue)}`;
    }

    if (historyItem.changeType === 'FIELD_UPDATE') {
      if (historyItem.fieldName) {
        const fieldDisplayName = this.getFieldDisplayName(historyItem.fieldName);
        return `${fieldDisplayName} changed from "${this.formatFieldValue(historyItem.oldValue)}" to "${this.formatFieldValue(historyItem.newValue)}"`;
      } else {
        return `Assigned from ${this.formatFieldValue(historyItem.oldValue)} to ${this.formatFieldValue(historyItem.newValue)}`;
      }
    }

    return 'Change made';
  }

  formatFieldValue(value: string | null): string {
    if (!value) return 'N/A';
    return value.replace(/_/g, ' ');
  }

  getFieldDisplayName(fieldName: string): string {
    const fieldMap: { [key: string]: string } = {
      'TITLE': 'Title',
      'DESCRIPTION': 'Description',
      'STATUS': 'Status',
      'SEVERITY': 'Severity',
      'PRIORITY': 'Priority'
    };
    return fieldMap[fieldName] || fieldName;
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

  getUserInitials(username: string | undefined): string {
    if (!username) return '??';
    const parts = username.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return username.substring(0, 2).toUpperCase();
  }
}

