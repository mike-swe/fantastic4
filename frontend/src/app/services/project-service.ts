import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Project } from '../interfaces/project';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  constructor(private http: HttpClient){}
  
  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${API_BASE_URL}/projects`);
  }
  getProjectById(id: string): Observable<Project>{
    return this.http.get<Project>(`${API_BASE_URL}/projects/${id}`);
  }

  getUserProjects(userId: string): Observable<Project[]>{
    return this.http.get<Project[]>(`${API_BASE_URL}/users/${userId}/projects`);
  }
  createProject(project: Project): Observable<Project> {
    return this.http.post<Project>(`${API_BASE_URL}/projects`, project);
  }

  updateProject(id: string, project: Project): Observable<Project> {
     return this.http.put<Project>(`${API_BASE_URL}/projects/${id}`, project);
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/projects/${id}`);
  }
}
