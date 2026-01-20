import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UserService } from './user-service';
import { Role } from '../enum/role';
import { firstValueFrom } from 'rxjs';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        UserService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should fetch all users and map string roles to Enums', async () => {
    // 1. Prepare raw data (like it comes from the DB)
    const rawUsers = [
      { username: 'alice', role: 'ADMIN' },
      { username: 'bob', role: 'developer' } // testing case-insensitivity
    ];

    const promise = firstValueFrom(service.getAllUsers());

    const req = httpMock.expectOne(`${baseUrl}/users`);
    expect(req.request.method).toBe('GET');

    // 2. Flush raw data
    req.flush(rawUsers);

    // 3. Verify transformation logic
    const result = await promise;
    expect(result[0].role).toBe(Role.ADMIN);
    expect(result[1].role).toBe(Role.DEVELOPER);
  });

  it('should fetch project users with correct URL', () => {
    const projectId = 'p123';
    service.getProjectUsers(projectId).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/users/projects/${projectId}/users`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should assign a user to a project via POST', () => {
    const pId = 'project-1';
    const uId = 'user-1';
    service.assignUserToProject(pId, uId).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/projects/${pId}/assign/${uId}`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({}); // Service sends empty object
    req.flush({});
  });

  it('should insert a user with correct payload and responseType text', () => {
    service.insertUser('testuser', 'pw123', 'test@test.com', 'ADMIN').subscribe();

    const req = httpMock.expectOne(`${baseUrl}/users/create-account`);
    expect(req.request.method).toBe('POST');

    // Verify payload structure
    expect(req.request.body).toEqual({
      username: 'testuser',
      password: 'pw123',
      email: 'test@test.com',
      role: 'ADMIN'
    });

    // Verify responseType is text (crucial for some backends)
    expect(req.request.responseType).toBe('text');

    req.flush('User created successfully');
  });

  it('should default to Role.TESTER if role is unknown', async () => {
    const rawUsers = [{ username: 'stranger', role: 'GHOST' }];
    const promise = firstValueFrom(service.getAllUsers());

    httpMock.expectOne(`${baseUrl}/users`).flush(rawUsers);

    const result = await promise;
    expect(result[0].role).toBe(Role.TESTER);
  });
});