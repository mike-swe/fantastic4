import { Component, EventEmitter, Output, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { IssueService } from '../../services/issue-service';
import { ProjectService } from '../../services/project-service';
import { Issues } from '../../interfaces/issues';
import { Project } from '../../interfaces/project';
import { AuthService } from '../../services/auth-service';
import { Role } from '../../enum/role';

@Component({
  selector: 'app-new-issue-modal',
  imports: [FormsModule, CommonModule],
  templateUrl: './new-issue-modal.html',
  styleUrl: './new-issue-modal.css',
})
export class NewIssueModal implements OnInit {
  @Output() close = new EventEmitter<void>();
  @Output() issueCreated = new EventEmitter<Issues>();

  title = '';
  description = '';
  selectedProjectId = '';
  selectedSeverity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' = 'MEDIUM';
  selectedPriority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' = 'MEDIUM';
  errorMessage = signal('');
  isLoading = signal(false);

  projects: Project[] = [];
  severityOptions: ('LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL')[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  priorityOptions: ('LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL')[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  constructor(
    private issueService: IssueService,
    private projectService: ProjectService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    const currentUser = this.authService.getCurrentUser();
    const userRole = currentUser?.role ?? null;

    if (userRole === Role.ADMIN) {
      this.projectService.getAllProjects().subscribe({
        next: (projects) => {
          this.projects = projects;
        },
        error: (error) => {
          console.error('Error loading projects:', error);
        }
      });
    } else if (userRole === Role.TESTER || userRole === Role.DEVELOPER) {
      if (currentUser?.id) {
        this.projectService.getUserProjects(currentUser.id).subscribe({
          next: (projects) => {
            this.projects = projects;
          },
          error: (error) => {
            console.error('Error loading user projects:', error);
          }
        });
      }
    }
  }

  onSubmit(): void {
    this.errorMessage.set('');
    
    if (!this.title || !this.title.trim()) {
      this.errorMessage.set('Title is required');
      return;
    }

    if (!this.description || !this.description.trim()) {
      this.errorMessage.set('Description is required');
      return;
    }

    if (!this.selectedProjectId) {
      this.errorMessage.set('Please select a project');
      return;
    }

    const selectedProject = this.projects.find(p => p.id === this.selectedProjectId);
    if (!selectedProject) {
      this.errorMessage.set('Invalid project selected');
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage.set('User not authenticated');
      return;
    }

    const newIssue: Partial<Issues> = {
      title: this.title.trim(),
      description: this.description.trim(),
      severity: this.selectedSeverity,
      priority: this.selectedPriority,
      status: 'OPEN',
      project: selectedProject,
      createdBy: currentUser,
      assignedTo: null,
    };

    this.isLoading.set(true);

    this.issueService.createIssue(newIssue as Issues).subscribe({
      next: (createdIssue) => {
        this.isLoading.set(false);
        this.resetForm();
        this.issueCreated.emit(createdIssue);
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Error creating issue:', error);
        if (error.error?.message) {
          this.errorMessage.set(error.error.message);
        } else if (error.status === 403) {
          this.errorMessage.set('Only Testers can create issues');
        } else {
          this.errorMessage.set('Failed to create issue. Please try again.');
        }
      }
    });
  }

  onCancel(): void {
    this.resetForm();
    this.close.emit();
  }

  onBackdropClick(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  private resetForm(): void {
    this.title = '';
    this.description = '';
    this.selectedProjectId = '';
    this.selectedSeverity = 'MEDIUM';
    this.selectedPriority = 'MEDIUM';
    this.errorMessage.set('');
  }
}

