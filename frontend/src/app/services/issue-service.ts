import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Issues } from '../interfaces/issues';


@Injectable({
  providedIn: 'root',
})

export class IssueService {

  constructor(private http: HttpClient){}


  getAllIssues(): Observable<Issues[]> {
    return this.http.get<Issues[]>("/issues");
  }

  getIssuesByUser(userId: string): Observable<Issues[]>{
    return this.http.get<Issues[]>(`/issues/user/${userId}`);

  }
}
