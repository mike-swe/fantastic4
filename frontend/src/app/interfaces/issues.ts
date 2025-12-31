import { Project } from "./project";
import { User } from "./user";
 
export interface Issues {
    id: string;
    title: string;
    description: string;
    status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
    severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    project: Project;  
    createdBy: User;
    assignedTo?: User | null;
    createdAt: string;
    updatedAt?: string;  
}
