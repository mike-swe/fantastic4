import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Issues } from '../interfaces/issues';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})

export class IssueService {

  constructor(private http: HttpClient){}


  getAllIssues(): Observable<Issues[]> {
    return this.http.get<Issues[]>(`${API_BASE_URL}/issues`);
  }

  getIssuesByUser(userId: string): Observable<Issues[]>{
    return this.http.get<Issues[]>(`${API_BASE_URL}/issues/user/${userId}`);
  }

  createIssue(issue: Issues): Observable<Issues> {
    return this.http.post<Issues>(`${API_BASE_URL}/issues`, issue);
  }

  updateIssueStatus(issueId: string, status: string): Observable<Issues> {
    return this.http.put<Issues>(`${API_BASE_URL}/issues/${issueId}/status`, { status });
  }

  getAssignedIssues(developerId: string): Observable<Issues[]> {
    return this.http.get<Issues[]>(`${API_BASE_URL}/issues/assigned/${developerId}`);
  }
}
