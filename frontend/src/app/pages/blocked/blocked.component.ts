import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SaasBlockService } from '../../core/services/saas-block.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-blocked-page',
  template: `
    <section class="blocked-page">
      <div class="blocked-card">
        <div class="blocked-highlight">
          <span class="blocked-badge">Smart Pet • Bloqueio SaaS</span>
          <h2>Acesso temporariamente indisponível</h2>
          <p>
            Identificamos uma restrição de assinatura ou cobrança na loja vinculada a este acesso.
          </p>
        </div>

        <div class="blocked-content">
          <div>
            <span class="section-mini-label">Status da conta</span>
            <h3>{{ state.title || 'Acesso bloqueado' }}</h3>
            <p>{{ state.description }}</p>
          </div>

          <div class="blocked-reason-box">
            <span class="blocked-reason-label">Mensagem retornada pelo backend</span>
            <div class="blocked-reason-value">
              {{ state.backendMessage || 'Bloqueio identificado pelo sistema.' }}
            </div>
          </div>

          <div class="blocked-actions">
            <button type="button" class="primary-action-btn" (click)="goHome()">
              Voltar ao início
            </button>

            <button type="button" class="ghost-btn" (click)="logout()">
              Sair da sessão
            </button>
          </div>
        </div>
      </div>
    </section>
  `
})
export class BlockedPageComponent {
  constructor(
    private router: Router,
    private auth: AuthService,
    private saasBlockService: SaasBlockService
  ) {}

  get state() {
    return this.saasBlockService.snapshot;
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  logout(): void {
    this.saasBlockService.clear();
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}