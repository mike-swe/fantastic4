import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../services/project-service';
import { Project } from '../../interfaces/project';

@Component({
  selector: 'app-new-project-modal',
  imports: [FormsModule],
  templateUrl: './new-project-modal.html',
  styleUrl: './new-project-modal.css',
})
export class NewProjectModal implements OnChanges {
  @Input() project: Project | null = null;
  @Input() isEditMode: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() projectCreated = new EventEmitter<Project>();
  @Output() projectUpdated = new EventEmitter<Project>();

  projectName = '';
  projectDescription = '';
  errorMessage = signal('');
  isLoading = signal(false);

  constructor(private projectService: ProjectService) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Populate form when project is provided for editing
    if (this.project && this.isEditMode) {
      this.projectName = this.project.name || '';
      this.projectDescription = this.project.description || '';
    } else if (!this.isEditMode) {
      // Reset form when in create mode
      this.resetForm();
    }
  }

  onSubmit(): void {
    this.errorMessage.set('');
    
    // Validate form
    if (!this.projectName || !this.projectName.trim()) {
      this.errorMessage.set('Project name is required');
      return;
    }

    // Trim whitespace
    const trimmedName = this.projectName.trim();
    const trimmedDescription = this.projectDescription.trim();

    this.isLoading.set(true);

    if (this.isEditMode && this.project) {
      // Update existing project
      const updatedProject: Partial<Project> = {
        id: this.project.id,
        name: trimmedName,
        description: trimmedDescription || undefined,
        status: this.project.status, // Keep existing status
      };

      this.projectService.updateProject(this.project.id, updatedProject as Project).subscribe({
        next: (updated) => {
          this.isLoading.set(false);
          this.resetForm();
          this.projectUpdated.emit(updated);
        },
        error: (error) => {
          this.isLoading.set(false);
          console.error('Error updating project:', error);
          if (error.error?.message) {
            this.errorMessage.set(error.error.message);
          } else if (error.status === 403) {
            this.errorMessage.set('Only Admin users can update projects');
          } else {
            this.errorMessage.set('Failed to update project. Please try again.');
          }
        }
      });
    } else {
      // Create new project (backend will set status to ACTIVE and createdBy from JWT)
    const newProject: Partial<Project> = {
      name: trimmedName,
      description: trimmedDescription || undefined,
    };

    this.projectService.createProject(newProject as Project).subscribe({
      next: (createdProject) => {
        this.isLoading.set(false);
        this.resetForm();
        this.projectCreated.emit(createdProject);
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Error creating project:', error);
        if (error.error?.message) {
          this.errorMessage.set(error.error.message);
        } else if (error.status === 403) {
          this.errorMessage.set('Only Admin users can create projects');
        } else {
          this.errorMessage.set('Failed to create project. Please try again.');
        }
      }
    });
    }
  }

  onCancel(): void {
    this.resetForm();
    this.close.emit();
  }

  onBackdropClick(event: Event): void {
    // Close modal if clicking on the backdrop (not the modal content)
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }

  private resetForm(): void {
    this.projectName = '';
    this.projectDescription = '';
    this.errorMessage.set('');
  }
}

