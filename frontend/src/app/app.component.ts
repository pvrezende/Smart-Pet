import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  template: `
    <ng-container *ngIf="isAuthRoute; else privateLayout">
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
  `
})
export class AppComponent {
  constructor(
    public auth: AuthService,
    private router: Router
  ) {}

  get isAuthRoute(): boolean {
    return this.router.url.startsWith('/login');
  }
}