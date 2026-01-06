import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Comment } from '../interfaces/comment';

const API_BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root',
})
export class CommentService {

  constructor(private http: HttpClient) {}

  getCommentsByIssue(issueId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API_BASE_URL}/issues/${issueId}/comments`);
  }

  createComment(issueId: string, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${API_BASE_URL}/issues/${issueId}/comments`, { content });
  }

  updateComment(issueId: string, commentId: string, content: string): Observable<Comment> {
    return this.http.put<Comment>(`${API_BASE_URL}/issues/${issueId}/comments/${commentId}`, { content });
  }

  deleteComment(issueId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/issues/${issueId}/comments/${commentId}`);
  }
}

