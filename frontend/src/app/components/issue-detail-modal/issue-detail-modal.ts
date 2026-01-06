import { Component, Input, OnInit, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Issues } from '../../interfaces/issues';
import { IssueHistory } from '../../interfaces/issue-history';
import { Comment } from '../../interfaces/comment';
import { IssueService } from '../../services/issue-service';
import { CommentService } from '../../services/comment-service';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';

@Component({
  selector: 'app-issue-detail-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './issue-detail-modal.html',
  styleUrl: './issue-detail-modal.css',
})
export class IssueDetailModal implements OnInit {
  @Input() issue: Issues | null = null;
  @Output() close = new EventEmitter<void>();

  history: IssueHistory[] = [];
  isLoadingHistory = signal(false);
  errorMessage = signal('');

  comments: Comment[] = [];
  isLoadingComments = signal(false);
  commentErrorMessage = signal('');
  newCommentText = '';
  editingCommentId: string | null = null;
  editingCommentText = '';

  constructor(
    private issueService: IssueService,
    private commentService: CommentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.issue) {
      this.loadHistory();
      this.loadComments();
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

  loadComments(): void {
    if (!this.issue?.id) {
      return;
    }

    this.isLoadingComments.set(true);
    this.commentErrorMessage.set('');

    this.commentService.getCommentsByIssue(this.issue.id).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoadingComments.set(false);
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.commentErrorMessage.set('Failed to load comments');
        this.isLoadingComments.set(false);
      }
    });
  }

  createComment(): void {
    if (!this.issue?.id || !this.newCommentText.trim()) {
      return;
    }

    this.commentErrorMessage.set('');
    const content = this.newCommentText.trim();

    this.commentService.createComment(this.issue.id, content).subscribe({
      next: (comment) => {
        this.comments.push(comment);
        this.newCommentText = '';
      },
      error: (error) => {
        console.error('Error creating comment:', error);
        this.commentErrorMessage.set(error.error?.message || 'Failed to create comment');
      }
    });
  }

  startEditing(comment: Comment): void {
    this.editingCommentId = comment.id;
    this.editingCommentText = comment.content;
  }

  cancelEditing(): void {
    this.editingCommentId = null;
    this.editingCommentText = '';
  }

  updateComment(commentId: string): void {
    if (!this.issue?.id || !this.editingCommentText.trim()) {
      return;
    }

    this.commentErrorMessage.set('');
    const content = this.editingCommentText.trim();

    this.commentService.updateComment(this.issue.id, commentId, content).subscribe({
      next: (updatedComment) => {
        const index = this.comments.findIndex(c => c.id === commentId);
        if (index !== -1) {
          this.comments[index] = updatedComment;
        }
        this.cancelEditing();
      },
      error: (error) => {
        console.error('Error updating comment:', error);
        this.commentErrorMessage.set(error.error?.message || 'Failed to update comment');
      }
    });
  }

  deleteComment(commentId: string): void {
    if (!this.issue?.id) {
      return;
    }

    if (!confirm('Are you sure you want to delete this comment?')) {
      return;
    }

    this.commentErrorMessage.set('');

    this.commentService.deleteComment(this.issue.id, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
      },
      error: (error) => {
        console.error('Error deleting comment:', error);
        this.commentErrorMessage.set(error.error?.message || 'Failed to delete comment');
      }
    });
  }

  canEditComment(comment: Comment): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    return comment.author.id === currentUser.id;
  }

  canDeleteComment(comment: Comment): boolean {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return false;
    return comment.author.id === currentUser.id || currentUser.role === Role.ADMIN;
  }
}

