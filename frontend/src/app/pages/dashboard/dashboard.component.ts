import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Dashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard-page',
  template: `
    <section class="dashboard-page dashboard-page-fit" *ngIf="dashboard; else loadingState">
      <div class="dashboard-fit-top">
        <div class="dashboard-fit-hero">
          <div class="hero-content">
            <span class="hero-badge">Smart Pet • Painel Gerencial</span>
            <h1 class="hero-title">Dashboard Executivo</h1>
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

          <article class="premium-metric-card premium-metric-card-fit metric-amber">
            <div class="metric-top">
              <div class="metric-icon-wrap">⚠️</div>
              <span class="metric-chip">Atenção</span>
            </div>
            <div class="metric-main-value">{{ dashboard.lowStockCount }}</div>
            <div class="metric-title">Alertas de estoque</div>
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
          <article class="insight-card insight-card-fit">
            <div class="insight-card-header insight-card-header-fit">
              <div>
                <span class="section-mini-label">Monitoramento</span>
                <h3>Alertas de estoque</h3>
              </div>
              <div class="alert-counter-badge">{{ dashboard.lowStockCount }}</div>
            </div>

            <div class="alert-status-panel alert-status-panel-fit">
              <div class="alert-status-icon">⚠️</div>
              <div class="alert-status-content">
                <span class="alert-status-label">Produtos com atenção</span>
                <strong class="alert-status-value">{{ dashboard.lowStockCount }}</strong>
                <p class="alert-status-text">
                  Área preparada para receber a tabela detalhada de produtos críticos.
                </p>
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

    <ng-template #loadingState>
      <section class="dashboard-page dashboard-page-fit">
        <div class="hero-panel">
          <div class="hero-content">
            <span class="hero-badge">Smart Pet • Painel Gerencial</span>
            <h1 class="hero-title">Dashboard Executivo</h1>
            <p class="hero-subtitle">Carregando os indicadores principais do sistema...</p>
          </div>
        </div>
      </section>
    </ng-template>
  `
})
export class DashboardPageComponent implements OnInit {
  dashboard?: Dashboard;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.api.getDashboard().subscribe(data => this.dashboard = data);
  }
}