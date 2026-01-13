import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth-service';
import { provideHttpClient } from '@angular/common/http';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController)

  });

  it('should retuyrn a token on successful login', () => {
    const mockResponse = {token: 'matikanetannhauser'};

    service.login('test' , 'password123').subscribe(res => {
      expect(res.token).toBe('matikanetannhauser');
    })

    const req = httpMock.expectOne('http://localhost:8080/users/login')
    req.flush(mockResponse);
  });

  afterEach(() => {
    httpMock.verify();
  });
});
