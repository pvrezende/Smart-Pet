import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  template: `
    <ng-container *ngIf="auth.isSessionReady(); else loadingState">
      <ng-container *ngIf="isPublicRoute; else privateLayout">
        <router-outlet></router-outlet>
      </ng-container>

      <ng-template #privateLayout>
        <div class="page-shell">
          <app-navbar *ngIf="auth.isAuthenticated()"></app-navbar>

          <main class="page-shell-main">
            <div class="container">
              <router-outlet></router-outlet>
            </div>
          </main>
        </div>
      </ng-template>
    </ng-container>

    <ng-template #loadingState>
      <section class="session-loading">
        <div class="card session-loading-card">
          <h3>Preparando sua sessão...</h3>
          <p>Estamos validando seu acesso e carregando as permissões do usuário.</p>
        </div>
      </section>
    </ng-template>
  `
})
export class AppComponent implements OnInit {
  constructor(
    public auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.auth.bootstrapSession();
  }

  get isPublicRoute(): boolean {
    return this.router.url.startsWith('/login') || this.router.url.startsWith('/blocked');
  }
}