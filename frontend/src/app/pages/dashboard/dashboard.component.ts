import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Dashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard-page',
  template: `
    <section class="dashboard-page dashboard-page-fit" *ngIf="!isLoading && dashboard; else dashboardState">
      <div class="dashboard-fit-top">
        <div class="dashboard-fit-hero">
          <div class="hero-content">
            <span class="hero-badge">Smart Pet • Painel Gerencial</span>
            <h1 class="hero-title">{{ greeting }}, bem-vindo ao Dashboard</h1>
            <p class="hero-subtitle">
              Visualize os principais indicadores do sistema, acompanhe o estoque e tome decisões com mais rapidez.
            </p>

            <div class="hero-highlights hero-highlights-compact">
              <div class="hero-highlight">
                <span class="hero-highlight-label">Produtos</span>
                <strong>{{ dashboard.totalProducts }}</strong>
              </div>
              <div class="hero-highlight">
                <span class="hero-highlight-label">Clientes</span>
                <strong>{{ dashboard.totalCustomers }}</strong>
              </div>
              <div class="hero-highlight">
                <span class="hero-highlight-label">Vendas</span>
                <strong>{{ dashboard.salesCount }}</strong>
              </div>
            </div>
          </div>

          <div class="hero-side-card hero-side-card-compact">
            <span class="hero-side-label">Valor total em estoque</span>
            <strong class="hero-side-value">R$ {{ dashboard.stockValue | number:'1.2-2' }}</strong>
            <div class="hero-side-divider"></div>
            <span class="hero-side-alert-label">Alertas de estoque</span>
            <strong class="hero-side-alert-value">{{ dashboard.lowStockCount }}</strong>
          </div>
        </div>
      </div>

      <div class="dashboard-fit-main">
        <div class="dashboard-top-actions">
          <div class="dashboard-last-update">
            Última atualização: {{ lastUpdatedLabel }}
          </div>

          <button type="button" class="ghost-btn ghost-btn-sm" (click)="loadDashboard()">
            Atualizar dashboard
          </button>
        </div>

        <div class="premium-metrics-grid premium-metrics-grid-fit">
          <article class="premium-metric-card premium-metric-card-fit metric-blue">
            <div class="metric-top">
              <div class="metric-icon-wrap">📦</div>
              <span class="metric-chip">Cadastros</span>
            </div>
            <div class="metric-main-value">{{ dashboard.totalProducts }}</div>
            <div class="metric-title">Produtos cadastrados</div>
          </article>

          <article class="premium-metric-card premium-metric-card-fit metric-emerald">
            <div class="metric-top">
              <div class="metric-icon-wrap">💰</div>
              <span class="metric-chip">Financeiro</span>
            </div>
            <div class="metric-main-value">R$ {{ dashboard.stockValue | number:'1.2-2' }}</div>
            <div class="metric-title">Valor do estoque</div>
          </article>

          <article class="premium-metric-card premium-metric-card-fit" [ngClass]="alertMetricClass">
            <div class="metric-top">
              <div class="metric-icon-wrap">{{ dashboard.lowStockCount > 0 ? '⚠️' : '✅' }}</div>
              <span class="metric-chip">{{ dashboard.lowStockCount > 0 ? 'Atenção' : 'Controle' }}</span>
            </div>
            <div class="metric-main-value">{{ dashboard.lowStockCount }}</div>
            <div class="metric-title">{{ dashboard.lowStockCount > 0 ? 'Alertas de estoque' : 'Estoque monitorado' }}</div>
          </article>

          <article class="premium-metric-card premium-metric-card-fit metric-violet">
            <div class="metric-top">
              <div class="metric-icon-wrap">👥</div>
              <span class="metric-chip">Relacionamento</span>
            </div>
            <div class="metric-main-value">{{ dashboard.totalCustomers }}</div>
            <div class="metric-title">Clientes cadastrados</div>
          </article>

          <article class="premium-metric-card premium-metric-card-fit metric-cyan">
            <div class="metric-top">
              <div class="metric-icon-wrap">🛒</div>
              <span class="metric-chip">Comercial</span>
            </div>
            <div class="metric-main-value">{{ dashboard.salesCount }}</div>
            <div class="metric-title">Vendas registradas</div>
          </article>

          <article class="premium-metric-card premium-metric-card-fit metric-dark">
            <div class="metric-top">
              <div class="metric-icon-wrap">🏪</div>
              <span class="metric-chip">Sistema</span>
            </div>
            <div class="metric-main-value metric-brand">Smart Pet</div>
            <div class="metric-title">Painel administrativo</div>
          </article>
        </div>

        <div class="dashboard-bottom-grid dashboard-bottom-grid-fit">
          <article
            class="insight-card insight-card-fit insight-card-clickable"
            (click)="goToLowStockProducts()"
            [class.insight-card-disabled]="dashboard.lowStockCount === 0"
          >
            <div class="insight-card-header insight-card-header-fit">
              <div>
                <span class="section-mini-label">Monitoramento</span>
                <h3>{{ alertTitle }}</h3>
              </div>
              <div class="alert-counter-badge" [class.alert-counter-badge-ok]="dashboard.lowStockCount === 0">
                {{ dashboard.lowStockCount }}
              </div>
            </div>

            <div class="alert-status-panel alert-status-panel-fit" [class.alert-status-panel-ok]="dashboard.lowStockCount === 0">
              <div class="alert-status-icon">{{ dashboard.lowStockCount > 0 ? '⚠️' : '✅' }}</div>
              <div class="alert-status-content">
                <span class="alert-status-label">{{ dashboard.lowStockCount > 0 ? 'Produtos com atenção' : 'Situação do estoque' }}</span>
                <strong class="alert-status-value">{{ dashboard.lowStockCount > 0 ? dashboard.lowStockCount : 'OK' }}</strong>
                <p class="alert-status-text">{{ alertMessage }}</p>
                <small class="alert-status-link-hint" *ngIf="dashboard.lowStockCount > 0">
                  Clique para abrir a lista de produtos com estoque baixo.
                </small>
              </div>
            </div>
          </article>

          <article class="insight-card insight-card-dark insight-card-fit">
            <div class="insight-card-header insight-card-header-fit">
              <div>
                <span class="section-mini-label">Resumo</span>
                <h3>Visão estratégica</h3>
              </div>
            </div>

            <div class="strategy-list strategy-list-fit">
              <div class="strategy-item">
                <span>Produtos ativos</span>
                <strong>{{ dashboard.totalProducts }}</strong>
              </div>
              <div class="strategy-item">
                <span>Clientes na base</span>
                <strong>{{ dashboard.totalCustomers }}</strong>
              </div>
              <div class="strategy-item">
                <span>Vendas registradas</span>
                <strong>{{ dashboard.salesCount }}</strong>
              </div>
              <div class="strategy-item">
                <span>Valor em estoque</span>
                <strong>R$ {{ dashboard.stockValue | number:'1.2-2' }}</strong>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <ng-template #dashboardState>
      <section class="dashboard-page dashboard-page-fit">
        <div *ngIf="isLoading" class="card dashboard-state-card">
          <h3>Carregando dashboard...</h3>
          <p>Buscando os indicadores principais do sistema.</p>
        </div>

        <div *ngIf="!isLoading && hasError" class="card dashboard-state-card dashboard-state-card-error">
          <h3>Não foi possível carregar a dashboard</h3>
          <p>Verifique a conexão com o backend e tente novamente.</p>
          <button type="button" class="ghost-btn ghost-btn-sm" (click)="loadDashboard()">Tentar novamente</button>
        </div>
      </section>
    </ng-template>
  `
})
export class DashboardPageComponent implements OnInit {
  dashboard?: Dashboard;
  isLoading = false;
  hasError = false;
  lastUpdatedAt?: Date;

  constructor(
    private api: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.hasError = false;

    this.api.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.lastUpdatedAt = new Date();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar dashboard:', error);
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  goToLowStockProducts(): void {
    if (!this.dashboard || this.dashboard.lowStockCount === 0) {
      return;
    }

    this.router.navigate(['/products'], {
      queryParams: { stockFilter: 'low' }
    });
  }

  get greeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) return 'Bom dia';
    if (hour < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  get lastUpdatedLabel(): string {
    if (!this.lastUpdatedAt) {
      return 'Ainda não atualizada';
    }

    return this.lastUpdatedAt.toLocaleString('pt-BR');
  }

  get alertTitle(): string {
    if (!this.dashboard) return 'Alertas de estoque';
    return this.dashboard.lowStockCount > 0
      ? 'Alertas de estoque'
      : 'Estoque sob controle';
  }

  get alertMessage(): string {
    if (!this.dashboard) return '';

    if (this.dashboard.lowStockCount === 0) {
      return 'Nenhum produto está em situação crítica no momento. O estoque está saudável e sob controle.';
    }

    if (this.dashboard.lowStockCount <= 2) {
      return 'Há poucos itens exigindo atenção. Vale acompanhar para evitar ruptura de abastecimento.';
    }

    return 'Existem vários produtos com estoque baixo. Recomendamos agir rápido para evitar perda de vendas.';
  }

  get alertMetricClass(): string {
    if (!this.dashboard) return 'metric-amber';
    return this.dashboard.lowStockCount > 0 ? 'metric-amber' : 'metric-emerald';
  }
}