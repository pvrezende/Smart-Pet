import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login-page',
  template: `
    <section class="auth-page">
      <div class="auth-brand-panel">
        <div class="auth-brand-card">
          <span class="auth-brand-badge">Smart Pet • Plataforma</span>
          <h1>Gestão moderna para sua operação pet.</h1>
          <p>
            Entre no sistema para acompanhar indicadores, controlar estoque, registrar vendas
            e evoluir sua gestão com uma base pronta para crescer.
          </p>

          <div class="auth-brand-highlights">
            <div class="auth-brand-highlight">
              <strong>Dashboard</strong>
              <span>Indicadores rápidos para tomada de decisão.</span>
            </div>
            <div class="auth-brand-highlight">
              <strong>Operação</strong>
              <span>Produtos, clientes e vendas no mesmo fluxo.</span>
            </div>
            <div class="auth-brand-highlight">
              <strong>Escalável</strong>
              <span>Base pronta para multi-loja e integrações futuras.</span>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-form-panel">
        <div class="auth-card">
          <div class="auth-card-header">
            <h2>Entrar no sistema</h2>
            <p>Use suas credenciais para acessar o painel administrativo.</p>
          </div>

          <form class="auth-form" (ngSubmit)="submit()">
            <div class="form-field">
              <label for="username">Usuário</label>
              <input
                id="username"
                [(ngModel)]="username"
                name="username"
                type="text"
                placeholder="Digite seu usuário"
                required
              >
            </div>

            <div class="form-field">
              <label for="password">Senha</label>
              <input
                id="password"
                [(ngModel)]="password"
                name="password"
                type="password"
                placeholder="Digite sua senha"
                required
              >
            </div>

            <div *ngIf="errorMessage" class="auth-error">
              {{ errorMessage }}
            </div>

            <div class="auth-form-actions">
              <button type="submit" [disabled]="isLoading">
                {{ isLoading ? 'Entrando...' : 'Entrar' }}
              </button>
            </div>

            <div class="auth-demo-box">
              <strong>Acesso para teste:</strong><br>
              Usuário: <strong>admin</strong><br>
              Senha: <strong>admin123</strong>
            </div>
          </form>
        </div>
      </div>
    </section>
  `
})
export class LoginPageComponent {
  username = '';
  password = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  submit(): void {
    this.errorMessage = '';

    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Informe usuário e senha para continuar.';
      return;
    }

    this.isLoading = true;

    this.auth.login({
      username: this.username.trim(),
      password: this.password
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Erro no login:', error);
        this.errorMessage = 'Não foi possível entrar. Verifique usuário, senha e backend.';
      }
    });
  }
}