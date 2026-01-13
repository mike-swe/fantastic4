import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth-service';
import { provideHttpClient } from '@angular/common/http';
import { JwtStorage } from './jwt-storage.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const mockJwtStorage = {
    setToken: vi.fn(),
    getToken: vi.fn(),
    clearToken: vi.fn()
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: JwtStorage, useValue: mockJwtStorage}
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController)

  });

  afterEach(() => {
    vi.clearAllMocks();
    httpMock.verify();
  });

  //UNIT TEST : login()
  it('should return a token on successful login', () => {
    const mockResponse = {token: 'matikanetannhauser'};
    

    service.login('mambo' , 'password123').subscribe(res => {
      expect(res.token).toBe('matikanetannhauser');
    })

    const req = httpMock.expectOne('http://localhost:8080/users/login')
    req.flush(mockResponse);

    expect(mockJwtStorage.setToken).toHaveBeenCalledWith("matikanetannhauser")
  });

  //UNIT TEST : logout()
  it('should clear the token from storage when logout is called', () => {
    service.logout();
    expect(mockJwtStorage.clearToken).toHaveBeenCalled();
  });

  //UNIT TEST : getToken()
  it('should get the token from storage when called', () => {
    service.getToken();
    expect(mockJwtStorage.getToken).toHaveBeenCalled();
  });


  //THIS TEST SHOULD FAIL
  it('this test should fail because 1 is not 2', () => {
  expect(1).toBe(2);
  });


});
