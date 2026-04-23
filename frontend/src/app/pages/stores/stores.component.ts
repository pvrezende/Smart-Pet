import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { CreateStorePayload, Store } from '../../models/store.model';

type StoreFilter = 'all' | 'active' | 'inactive';
type StoreSort = 'name' | 'recent';

@Component({
  selector: 'app-stores-page',
  template: `
    <section class="customers-page">
      <div class="section-heading">
        <div>
          <h2>Gestão de lojas</h2>
          <p>Cadastre, acompanhe e administre as lojas do ambiente SaaS.</p>
        </div>
        <button type="button" class="ghost-btn" (click)="load()">Atualizar lista</button>
      </div>

      <div *ngIf="feedbackMessage" class="card customers-feedback" [class.customers-feedback-error]="feedbackType === 'error'">
        <strong>{{ feedbackType === 'success' ? 'Sucesso:' : 'Atenção:' }}</strong>
        <span>{{ feedbackMessage }}</span>
      </div>

      <div class="customers-layout">
        <article class="customer-form-card">
          <div class="form-card-header">
            <span class="section-mini-label">SaaS</span>
            <h3>Nova loja</h3>
            <p>Cadastre uma nova unidade para começar a operar no sistema.</p>
          </div>

          <form class="customer-form-grid" (ngSubmit)="save()">
            <div class="form-field form-field-full">
              <label for="name">Nome da loja</label>
              <input
                id="name"
                [(ngModel)]="form.name"
                name="name"
                placeholder="Ex.: Loja Centro"
                required
              >
            </div>

            <div class="form-field">
              <label for="code">Código</label>
              <input
                id="code"
                [(ngModel)]="form.code"
                name="code"
                placeholder="Ex.: CENTRO"
                required
              >
            </div>

            <div class="form-field">
              <label for="phone">Telefone</label>
              <input
                id="phone"
                [(ngModel)]="form.phone"
                name="phone"
                placeholder="(92) 99999-9999"
                required
              >
            </div>

            <div class="form-field form-field-full">
              <label for="address">Endereço</label>
              <input
                id="address"
                [(ngModel)]="form.address"
                name="address"
                placeholder="Cidade, bairro, rua e número"
                required
              >
            </div>

            <div class="form-actions form-field-full">
              <button type="submit" class="primary-action-btn">Cadastrar loja</button>
            </div>
          </form>
        </article>

        <aside class="customer-summary-panel">
          <article class="summary-mini-card">
            <span class="summary-mini-label">Lojas cadastradas</span>
            <strong class="summary-mini-value">{{ stores.length }}</strong>
          </article>

          <article class="summary-mini-card">
            <span class="summary-mini-label">Lojas ativas</span>
            <strong class="summary-mini-value">{{ activeStoresCount }}</strong>
          </article>

          <article class="summary-mini-card summary-mini-card-highlight">
            <span class="summary-mini-label">Lojas inativas</span>
            <strong class="summary-mini-value">{{ inactiveStoresCount }}</strong>
          </article>
        </aside>
      </div>

      <article class="customers-table-card">
        <div class="customers-table-header">
          <div>
            <span class="section-mini-label">Ambiente SaaS</span>
            <h3>Lista de lojas</h3>
            <p>Gerencie as lojas já cadastradas na plataforma.</p>
          </div>
        </div>

        <div class="customers-toolbar">
          <div class="form-field">
            <label for="storeSearch">Buscar</label>
            <input
              id="storeSearch"
              [(ngModel)]="searchTerm"
              name="storeSearch"
              placeholder="Buscar por nome, código, telefone ou ID"
            >
          </div>

          <div class="form-field">
            <label for="storeFilter">Status</label>
            <select id="storeFilter" [(ngModel)]="storeFilter" name="storeFilter">
              <option value="all">Todas</option>
              <option value="active">Ativas</option>
              <option value="inactive">Inativas</option>
            </select>
          </div>

          <div class="form-field">
            <label for="storeSort">Ordenar por</label>
            <select id="storeSort" [(ngModel)]="sortOption" name="storeSort">
              <option value="name">Nome</option>
              <option value="recent">Mais recente</option>
            </select>
          </div>
        </div>

        <div class="customers-results-bar">
          <span>{{ filteredStores.length }} loja(s) encontrada(s)</span>
          <button type="button" class="ghost-btn ghost-btn-sm" (click)="clearFilters()">
            Limpar filtros
          </button>
        </div>

        <div class="table-responsive customers-table-responsive">
          <table>
            <thead>
              <tr>
                <th>Loja</th>
                <th>Código</th>
                <th>Telefone</th>
                <th>Endereço</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let store of filteredStores">
                <td>
                  <div class="table-customer-cell">
                    <strong>{{ store.name }}</strong>
                    <small>ID {{ store.id || '-' }}</small>
                  </div>
                </td>
                <td>{{ store.code }}</td>
                <td>{{ store.phone }}</td>
                <td>{{ store.address }}</td>
                <td>
                  <span class="badge" [ngClass]="store.active === false ? 'critical' : 'ok'">
                    {{ store.active === false ? 'Inativa' : 'Ativa' }}
                  </span>
                </td>
                <td>
                  <div class="inventory-actions">
                    <button
                      type="button"
                      class="table-action-btn danger"
                      (click)="deactivate(store)"
                      [disabled]="store.active === false"
                    >
                      Desativar
                    </button>
                  </div>
                </td>
              </tr>

              <tr *ngIf="!filteredStores.length">
                <td colspan="6">
                  <div class="empty-table-state">
                    <strong>Nenhuma loja encontrada com os filtros atuais.</strong>
                    <p>Ajuste a busca ou limpe os filtros para visualizar as lojas.</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  `
})
export class StoresPageComponent implements OnInit {
  stores: Store[] = [];

  form: CreateStorePayload = {
    name: '',
    code: '',
    address: '',
    phone: ''
  };

  searchTerm = '';
  storeFilter: StoreFilter = 'all';
  sortOption: StoreSort = 'name';

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getStores().subscribe({
      next: (data) => {
        this.stores = data;
      },
      error: () => {
        this.setError('Não foi possível carregar a lista de lojas.');
      }
    });
  }

  save(): void {
    this.clearFeedback();

    if (!this.form.name.trim()) {
      this.setError('Informe o nome da loja.');
      return;
    }

    if (!this.form.code.trim()) {
      this.setError('Informe o código da loja.');
      return;
    }

    if (!this.form.address.trim()) {
      this.setError('Informe o endereço da loja.');
      return;
    }

    if (!this.form.phone.trim()) {
      this.setError('Informe o telefone da loja.');
      return;
    }

    const payload: CreateStorePayload = {
      name: this.form.name.trim(),
      code: this.form.code.trim().toUpperCase(),
      address: this.form.address.trim(),
      phone: this.form.phone.trim()
    };

    this.api.createStore(payload).subscribe({
      next: () => {
        this.form = {
          name: '',
          code: '',
          address: '',
          phone: ''
        };
        this.load();
        this.setSuccess('Loja cadastrada com sucesso.');
      },
      error: () => {
        this.setError('Não foi possível cadastrar a loja.');
      }
    });
  }

  deactivate(store: Store): void {
    this.clearFeedback();

    if (!store.id) {
      this.setError('Loja inválida para desativação.');
      return;
    }

    this.api.deactivateStore(store.id).subscribe({
      next: () => {
        this.load();
        this.setSuccess(`Loja "${store.name}" desativada com sucesso.`);
      },
      error: () => {
        this.setError(`Não foi possível desativar a loja "${store.name}".`);
      }
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.storeFilter = 'all';
    this.sortOption = 'name';
  }

  get activeStoresCount(): number {
    return this.stores.filter(store => store.active !== false).length;
  }

  get inactiveStoresCount(): number {
    return this.stores.filter(store => store.active === false).length;
  }

  get filteredStores(): Store[] {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();

    const filtered = this.stores.filter(store => {
      const matchesSearch =
        !normalizedSearch ||
        store.name.toLowerCase().includes(normalizedSearch) ||
        store.code.toLowerCase().includes(normalizedSearch) ||
        store.phone.toLowerCase().includes(normalizedSearch) ||
        String(store.id || '').includes(normalizedSearch);

      const matchesFilter =
        this.storeFilter === 'all' ||
        (this.storeFilter === 'active' && store.active !== false) ||
        (this.storeFilter === 'inactive' && store.active === false);

      return matchesSearch && matchesFilter;
    });

    return filtered.sort((a, b) => {
      switch (this.sortOption) {
        case 'recent':
          return Number(b.id || 0) - Number(a.id || 0);
        case 'name':
        default:
          return a.name.localeCompare(b.name);
      }
    });
  }

  private setSuccess(message: string): void {
    this.feedbackType = 'success';
    this.feedbackMessage = message;
  }

  private setError(message: string): void {
    this.feedbackType = 'error';
    this.feedbackMessage = message;
  }

  private clearFeedback(): void {
    this.feedbackMessage = '';
  }
}