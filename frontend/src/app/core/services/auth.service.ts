import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginPayload, LoginResponse, AuthUser } from '../../models/auth.models';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(
    this.storage.getUser<AuthUser>()
  );

  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private storage: StorageService
  ) {}

  login(payload: LoginPayload): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, payload).pipe(
      tap((response) => {
        this.storage.setToken(response.token);

        const normalizedUser: AuthUser = {
          id: response.id,
          name: response.name || 'Usuário',
          username: response.username || payload.username,
          role: response.role,
          active: response.active,
          storeId: response.storeId,
          storeName: response.storeName
        };

        this.storage.setUser(normalizedUser);
        this.currentUserSubject.next(normalizedUser);
      })
    );
  }

  logout(): void {
    this.storage.clearAllAuth();
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return this.storage.getToken();
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  userName(): string {
    return this.getCurrentUser()?.name || this.getCurrentUser()?.username || 'Usuário';
  }

  storeName(): string {
    return this.getCurrentUser()?.storeName || 'Loja ativa';
  }
}