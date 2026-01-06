import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class JwtStorage {
  private readonly TOKEN_KEY = 'REVAISSUE_TOKEN';

  setToken(jwt: string): void {
    localStorage.setItem(this.TOKEN_KEY, jwt);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }
}
