import { TestBed } from '@angular/core/testing';

import { CommentService } from './comment-service';
import { Comment } from '../interfaces/comment';
import { User } from "../interfaces/user";
import { Role } from '../enum/role';

import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';



describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController
  const baseUrl = 'http://localhost:8080'

  /*
  id: string;
  username: string;
  email: string;
  role: Role;
  createdAt?: string;
  updatedAt?: string;
  */

  const mockUser: User = {
    id: 'user-123',
    username: 'test-user',
    email: 'test@example.com',
    role: Role.TESTER,
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z'
  };

  /*
  id: string;
  content: string;
  issue: {
    id: string;
  };
  author: User;
  createdAt: string;
  updatedAt: string;
  */

  const mockComment : Comment[] = [
    {
      id : '101',
      content : 'this is a test comment',
      issue : {
        id: '67'
      },
      author : mockUser,
      createdAt: '2024-05-20T10:00:00Z',
      updatedAt: '2024-05-20T10:00:00Z'
    }
  ];
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CommentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });


  it('should getCommentsByIssue', async () => {
    
    //Apparently its good to still test the result so first part is testing the getter
    const issueId = '67';
    const promise = firstValueFrom(service.getCommentsByIssue(issueId));

    const req = httpMock.expectOne(`${baseUrl}/issues/${issueId}/comments`);
    expect(req.request.method).toBe('GET');


    req.flush(mockComment);

    //Check if the data arrived intact
    const result = await promise;

    expect(result[0].issue.id).toBe(issueId);
  });

  it('should createComment', async () => {
    const issueId = 'newIssue';
    const content = 'new comment text';

    const mockResponse = {...mockComment[0], issueId:issueId, content:content}

    const promise = firstValueFrom(service.createComment(issueId, content));

    const req = httpMock.expectOne(`${baseUrl}/issues/${issueId}/comments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content });
    
    req.flush(mockResponse);

    const result = await promise;
    expect(result.content).toBe('content');


  });

  it('should updateComment', () => {
    expect(service).toBeTruthy();
  });

  it('should deleteComment', () => {
    expect(service).toBeTruthy();
  });

});
