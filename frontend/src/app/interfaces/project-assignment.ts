import { Project } from './project';
import { User } from './user';

export interface ProjectAssignment {
  id: string;
  project: Project;
  user: User;
  assignedAt: string;
}

