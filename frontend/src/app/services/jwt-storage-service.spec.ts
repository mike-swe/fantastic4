import { TestBed } from '@angular/core/testing';
import { JwtStorage } from './jwt-storage.service';

describe('JwtStorage', () => {
  let service: JwtStorage;
  const TOKEN_KEY = 'REVAISSUE_TOKEN';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JwtStorage]
    });
    service = TestBed.inject(JwtStorage);

    // Clear localStorage before every test to ensure isolation
    localStorage.clear();
  });

  it('should store the token in localStorage', () => {
    const testToken = 'fake-jwt-token';
    service.setToken(testToken);

    // Verify it was actually saved to the browser's storage
    expect(localStorage.getItem(TOKEN_KEY)).toBe(testToken);
  });

  it('should retrieve the token from localStorage', () => {
    const testToken = 'retrieved-token';
    localStorage.setItem(TOKEN_KEY, testToken);

    const result = service.getToken();
    expect(result).toBe(testToken);
  });

  it('should return null if there is no token', () => {
    const result = service.getToken();
    expect(result).toBeNull();
  });

  it('should remove the token from localStorage on clear', () => {
    localStorage.setItem(TOKEN_KEY, 'to-be-removed');

    service.clearToken();

    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });
});