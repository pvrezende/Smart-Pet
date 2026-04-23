import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginPayload,
  LoginResponse,
  AuthUser,
  MeResponse,
  UserRole
} from '../../models/auth.models';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;

  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(
    this.storage.getUser<AuthUser>()
  );
  currentUser$ = this.currentUserSubject.asObservable();

  private readonly sessionReadySubject = new BehaviorSubject<boolean>(false);
  sessionReady$ = this.sessionReadySubject.asObservable();

  constructor(
    private http: HttpClient,
    private storage: StorageService
  ) {}

  bootstrapSession(): void {
    const token = this.getToken();

    if (!token) {
      this.currentUserSubject.next(null);
      this.sessionReadySubject.next(true);
      return;
    }

    this.fetchMe().subscribe({
      next: (user) => {
        this.storage.setUser(user);
        this.currentUserSubject.next(user);
        this.sessionReadySubject.next(true);
      },
      error: () => {
        this.logout();
        this.sessionReadySubject.next(true);
      }
    });
  }

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
          storeId: response.storeId ?? null,
          storeName: response.storeName ?? null
        };

        this.storage.setUser(normalizedUser);
        this.currentUserSubject.next(normalizedUser);
        this.sessionReadySubject.next(true);
      })
    );
  }

  fetchMe(): Observable<AuthUser> {
    return this.http.get<MeResponse>(`${this.baseUrl}/auth/me`).pipe(
      tap((response) => {
        const normalizedUser: AuthUser = {
          id: response.id,
          name: response.name,
          username: response.username,
          role: response.role,
          active: response.active,
          storeId: response.storeId,
          storeName: response.storeName
        };

        this.storage.setUser(normalizedUser);
        this.currentUserSubject.next(normalizedUser);
      }),
      catchError((error) => {
        return of((() => { throw error; })() as never);
      })
    );
  }

  logout(): void {
    this.storage.clearAllAuth();
    this.currentUserSubject.next(null);
    this.sessionReadySubject.next(true);
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

  isSessionReady(): boolean {
    return this.sessionReadySubject.value;
  }

  hasRole(role: UserRole): boolean {
    return this.getCurrentUser()?.role === role;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const currentRole = this.getCurrentUser()?.role;
    return !!currentRole && roles.includes(currentRole);
  }

  userName(): string {
    const user = this.getCurrentUser();
    return user?.name || user?.username || 'Usuário';
  }

  roleLabel(): string {
    const role = this.getCurrentUser()?.role;

    switch (role) {
      case 'SUPER_ADMIN':
        return 'Super Admin';
      case 'ADMIN':
        return 'Admin';
      case 'ADMIN_STORE':
        return 'Admin da Loja';
      default:
        return 'Usuário';
    }
  }

  storeName(): string {
    return this.getCurrentUser()?.storeName || 'Loja ativa';
  }
}