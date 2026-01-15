import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth-service';
import { provideHttpClient } from '@angular/common/http';
import { JwtStorage } from './jwt-storage.service';
import { Role } from '../enum/role';

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

  //UNIT TEST : isAuthenticated()
  describe('isAuthenticated()', () => {

    //UNIT TEST 1: isAuthenticated()
    it('should return true if token is not expired', () => {
      mockJwtStorage.getToken.mockReturnValue('tokaiteio');
      const futureDate = new Date('2900-01-01').getTime()/1000
      vi.spyOn(service as any,'decodeToken').mockReturnValue({exp: futureDate});
      expect(service.isAuthenticated()).toBe(true);
    });

    //UNIT TEST 2: isAuthenticated()
    it('should return false if token is expired', () => {
      mockJwtStorage.getToken.mockReturnValue('tokaiteio');
      const futureDate = new Date('2000-01-01').getTime()/1000
      vi.spyOn(service as any,'decodeToken').mockReturnValue({exp: futureDate});
      expect(service.isAuthenticated()).toBe(false);
    });

    //UNIT TEST 3: isAuthenticated()
    it('should return false if there is no token/bad token', () => {
      mockJwtStorage.getToken.mockReturnValue('tokaiteio');
      
      vi.spyOn(service as any, 'decodeToken').mockImplementation(() => {
        throw new Error("badtoken")
      });

      const result = service.isAuthenticated();

      expect(result).toBe(false);
    });

  });

  //UNIT TEST : getCurrentUser()
  describe('getCurrentUser', () => {

    //UNIT TEST !token : getCurrentUser()
    it('should return null if no token', () => {
      mockJwtStorage.getToken.mockReturnValue(null)

      const result = service.getCurrentUser();

      expect(result).toBe(null);
    });

    //UNIT TEST Correct : getCurrentUser()
    it('should return user if token exists', () => {
      mockJwtStorage.getToken.mockReturnValue('kitasan');
      const mockPayload = {
        sub: 'id',
        username: 'goldship',
        role: 'ADMIN_ROLE'
      };
      vi.spyOn(service as any, 'decodeToken').mockReturnValue(mockPayload);
      vi.spyOn(service as any, 'mapRole').mockReturnValue('ADMIN');

      const result = service.getCurrentUser();

      expect(result).toEqual({
        id: 'id',
        username: 'goldship',
        email: '',
        role: 'ADMIN'
      });
      
    });

    //UNIT TEST Error: getCurrentUser()
    it('should return null if the token is bad', () =>{
      mockJwtStorage.getToken.mockReturnValue('kitasan');

      vi.spyOn(service as any, 'decodeToken').mockImplementation(() => {
        throw new Error("badtoken")
      });

      const result = service.getCurrentUser()
      expect(result).toBe(null)
    });
  });
  
  //UNIT TEST : decodeToken()
  describe('decodeToken()', () => {

    //UNIT TEST Success: decodeToken()
    it('should decode a valid base64 token string into an object', () => {
    // This is the Base64URL for {"sub":"123","username":"test"}
      const payload = 'eyJzdWIiOiIxMjMiLCJ1c2VybmFtZSI6InRlc3QifQ';
      const fakeJwt = `header.${payload}.signature`;

      // Accessing the private method
      const result = (service as any).decodeToken(fakeJwt);

      expect(result).toEqual({
        sub: '123',
        username: 'test'
      });
    });

    //UNIT TEST Error: decodeToken()
    it('should throw an "Invalid token" error if the string is malformed', () => {
      const badToken = 'not-a-jwt-at-all';

      // When testing for errors, we wrap the call in a function
      expect(() => (service as any).decodeToken(badToken)).toThrow('Invalid token');
    });
  });

  //UNIT TEST : mapRole()
  describe('mapRole()', () => {

    it('should return Role.ADMIN when string is "ADMIN"', () => {
      const result = (service as any).mapRole('ADMIN');
      expect(result).toBe(Role.ADMIN);
    });

    it('should return Role.TESTER when string is "TESTER"', () => {
      const result = (service as any).mapRole('TESTER');
      expect(result).toBe(Role.TESTER);
    });

    it('should return Role.DEVELOPER when string is "DEVELOPER"', () => {
      const result = (service as any).mapRole('DEVELOPER');
      expect(result).toBe(Role.DEVELOPER);
    });

    it('should return Role.TESTER as a default for unknown roles', () => {
      // This tests your 'else' block
      const result = (service as any).mapRole('GUEST');
      expect(result).toBe(Role.TESTER);
    });
  
});








  //THIS TEST SHOULD FAIL
  //it('this test should fail because 1 is not 2', () => {
  //expect(1).toBe(2);
  //});


});
