import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly tokenKey = 'smartpet_token';
  private readonly userKey = 'smartpet_user';

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  setUser(user: unknown): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  getUser<T>(): T | null {
    const raw = localStorage.getItem(this.userKey);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  clearUser(): void {
    localStorage.removeItem(this.userKey);
  }

  clearAllAuth(): void {
    this.clearToken();
    this.clearUser();
  }
}