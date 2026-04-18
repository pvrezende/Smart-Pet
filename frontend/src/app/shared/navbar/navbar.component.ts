import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  template: `
    <header class="navbar navbar-stacked">
      <div class="navbar-inner">
        <div class="navbar-brand-block">
          <h1 class="navbar-brand">Smart Pet</h1>
          <p class="navbar-subbrand">Sistema de gestão para pet shop</p>
        </div>

        <nav class="navbar-menu">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
            Dashboard
          </a>
          <a routerLink="/products" routerLinkActive="active">
            Produtos
          </a>
          <a routerLink="/customers" routerLinkActive="active">
            Clientes
          </a>
          <a routerLink="/sales" routerLinkActive="active">
            Vendas
          </a>
        </nav>

        <div class="topbar-actions">
          <div class="topbar-store-chip">
            {{ auth.storeName() }}
          </div>

          <button type="button" class="ghost-btn ghost-btn-sm" (click)="logout()">
            Sair
          </button>
        </div>
      </div>
    </header>
  `
})
export class NavbarComponent {
  constructor(
    public auth: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}