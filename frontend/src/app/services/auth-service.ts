import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { JwtStorage } from './jwt-storage.service';
import { User } from '../interfaces/user';
import { Role } from '../enum/role';

export interface TokenTransport {
  token: string;
}

interface JwtPayload {
  sub: string; 
  username: string;
  role: string;
  iat?: number;
  exp?: number;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(
    private http: HttpClient,
    private jwtStorage: JwtStorage
  ) {}

  login(username: string, password: string): Observable<TokenTransport> {
    return this.http.post<TokenTransport>('http://localhost:8080/users/login', {
      username,
      password
    }).pipe(
      tap(response => {
        if (response.token) {
          this.jwtStorage.setToken(response.token);
        }
      })
    );
  }

  logout(): void {
    this.jwtStorage.clearToken();
  }

  getToken(): string | null {
    return this.jwtStorage.getToken();
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    

    try {
      const payload = this.decodeToken(token);
      if (payload.exp) {
        const expirationTime = payload.exp * 1000; 
        return Date.now() < expirationTime;
      }
      return true; 
    } catch (error) {
      return false; 
    }
  }

  getCurrentUser(): User | null {
    const token = this.getToken();
    if (!token) {
      console.warn('AuthService: No token found');
      return null;
    }
  
    try {
      const payload = this.decodeToken(token);
      console.log('AuthService: Decoded payload:', payload);
      
      const user = {
        id: payload.sub,
        username: payload.username,
        email: '',
        role: this.mapRole(payload.role)
      };
      
      console.log('AuthService: Mapped user:', user);
      return user;
    } catch (error) {
      console.error('AuthService: Error decoding token:', error);
      return null;
    }
  }

  private decodeToken(token: string): JwtPayload {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Invalid token');
    }
  }

  private mapRole(roleString: string): Role {
    const upperRole = roleString.toUpperCase();
    if (upperRole === 'ADMIN') return Role.ADMIN;
    if (upperRole === 'TESTER') return Role.TESTER;
    if (upperRole === 'DEVELOPER') return Role.DEVELOPER;
    return Role.TESTER; 
  }

  createUserAccount(usernameInput: string, passwordInput: string): Observable<any> {
    return this.http.post('http://localhost:8080/users', {
      username: usernameInput,
      password: passwordInput
    });
  }
}
