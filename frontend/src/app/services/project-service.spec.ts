import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ProjectService } from './project-service';
import { Project } from '../interfaces/project';

describe('ProjectService', () => {
  let service: ProjectService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProjectService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ProjectService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should call GET for all projects', () => {
    service.getAllProjects().subscribe();
    const req = httpMock.expectOne(`${baseUrl}/projects`);
    expect(req.request.method).toBe('GET');
  });

  it('should call GET for a single project by id', () => {
    const id = 'pro-123';
    service.getProjectById(id).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/projects/${id}`);
    expect(req.request.method).toBe('GET');
  });

  it('should call GET for user-specific projects', () => {
    const userId = 'user-99';
    service.getUserProjects(userId).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/users/${userId}/projects`);
    expect(req.request.method).toBe('GET');
  });

  it('should call POST with project body to create', () => {
    const newProject = { name: 'New App' } as Project;
    service.createProject(newProject).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/projects`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newProject);
  });

  it('should call PUT with updated project body', () => {
    const id = 'pro-123';
    const updatedData = { name: 'Updated App' } as Project;
    service.updateProject(id, updatedData).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/projects/${id}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedData);
  });

  it('should call DELETE for a specific project', () => {
    const id = 'pro-123';
    service.deleteProject(id).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/projects/${id}`);
    expect(req.request.method).toBe('DELETE');
  });
});