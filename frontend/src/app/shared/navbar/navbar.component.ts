import { Component } from '@angular/core';

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
      </div>
    </header>
  `
})
export class NavbarComponent {}