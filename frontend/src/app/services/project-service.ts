import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Project } from '../interfaces/project';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  constructor(private http: HttpClient){}
  
  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>("/projects");
  }
  getProjectById(id: string): Observable<Project>{
    return this.http.get<Project>(`/projects/${id}`);
  }

  getUserProjects(userId: string): Observable<Project[]>{
    return this.http.get<Project[]>(`/users/${userId}/projects`);
  }
  createProject(project: Project): Observable<Project> {
    return this.http.post<Project>("/projects", project);
  }

  updateProject(id: string, project: Project): Observable<Project> {
     return this.http.put<Project>(`/projects/${id}`, project);
  }
}
