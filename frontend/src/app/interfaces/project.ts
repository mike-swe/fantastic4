import { Issues } from "./issues";
import { User } from "./user";


export interface Project {
    id: string; 
    name: string;
    description: string;
    status: 'ACTIVE' | 'ARCHIVED'; 
    createdBy: User;
    createdAt: string; 
    issues?: Issues[];  
    assignments?: any[]; 

}
