import { TestBed } from '@angular/core/testing';

import { CommentService } from './comment-service';
import { Comment } from '../interfaces/comment';
import { User } from "../interfaces/user";
import { Role } from '../enum/role';



describe('IssueService', () => {
  let service: CommentService;
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
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
