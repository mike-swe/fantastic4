import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { User } from '../interfaces/user';
import { Role } from '../enum/role';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_BASE_URL}/users`).pipe(
      map(users => users.map(user => ({
        ...user,
        role: this.mapRole(user.role as any)
      })))
    );
  }

  getProjectUsers(projectId: string): Observable<User[]> {
    return this.http.get<User[]>(`${API_BASE_URL}/users/projects/${projectId}/users`).pipe(
      map(users => users.map(user => ({
        ...user,
        role: this.mapRole(user.role as any)
      })))
    );
  }

  assignUserToProject(projectId: string, userId: string): Observable<any> {
    return this.http.post(`${API_BASE_URL}/projects/${projectId}/assign/${userId}`, {});
  }

  private mapRole(roleString: string | Role): Role {
    if (typeof roleString === 'number') {
      return roleString;
    }
    
    const upperRole = String(roleString).toUpperCase();
    if (upperRole === 'ADMIN') return Role.ADMIN;
    if (upperRole === 'TESTER') return Role.TESTER;
    if (upperRole === 'DEVELOPER') return Role.DEVELOPER;
    return Role.TESTER;
  }

  insertUser(userName: string, password: string , email: string , role: string): Observable<any> {
    const userPayload = {
      username: userName,
      password: password,
      email: email,
      role: role
    };

    return this.http.post(`${API_BASE_URL}/users/create-account`, userPayload, { responseType: 'text' })
  }
}
