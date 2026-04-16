import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Customer } from '../../models/customer.model';

type CustomerFilter = 'all' | 'complete' | 'basic' | 'inactive';
type CustomerSortOption = 'name' | 'recent';

@Component({
  selector: 'app-customers-page',
  template: `
    <section class="customers-page">
      <div class="section-heading">
        <div>
          <h2>Gestão de clientes</h2>
          <p>Cadastre, acompanhe e organize sua base de clientes do Smart Pet.</p>
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
            <span class="section-mini-label">Cadastro</span>
            <h3>Novo cliente</h3>
            <p>Preencha os dados principais para adicionar um novo cliente ao sistema.</p>
          </div>

          <form class="customer-form-grid" (ngSubmit)="save()">
            <div class="form-field form-field-full">
              <label for="name">Nome completo</label>
              <input
                id="name"
                [(ngModel)]="form.name"
                name="name"
                placeholder="Ex.: Maria Oliveira"
                required
              >
            </div>

            <div class="form-field">
              <label for="cpf">CPF</label>
              <input
                id="cpf"
                [(ngModel)]="form.cpf"
                name="cpf"
                placeholder="000.000.000-00"
              >
            </div>

            <div class="form-field">
              <label for="phone">Telefone</label>
              <input
                id="phone"
                [(ngModel)]="form.phone"
                name="phone"
                placeholder="(00) 00000-0000"
                required
              >
            </div>

            <div class="form-field">
              <label for="email">E-mail</label>
              <input
                id="email"
                [(ngModel)]="form.email"
                name="email"
                placeholder="cliente@email.com"
              >
            </div>

            <div class="form-field form-field-full">
              <label for="address">Endereço</label>
              <input
                id="address"
                [(ngModel)]="form.address"
                name="address"
                placeholder="Rua, número, bairro e complemento"
              >
            </div>

            <div class="form-actions form-field-full">
              <button type="submit" class="primary-action-btn">Salvar cliente</button>
            </div>
          </form>
        </article>

        <aside class="customer-summary-panel">
          <article class="summary-mini-card">
            <span class="summary-mini-label">Clientes cadastrados</span>
            <strong class="summary-mini-value">{{ customers.length }}</strong>
          </article>

          <article class="summary-mini-card">
            <span class="summary-mini-label">Com telefone</span>
            <strong class="summary-mini-value">{{ customersWithPhone }}</strong>
          </article>

          <article class="summary-mini-card summary-mini-card-highlight">
            <span class="summary-mini-label">Com e-mail</span>
            <strong class="summary-mini-value">{{ customersWithEmail }}</strong>
          </article>
        </aside>
      </div>

      <article class="customers-table-card">
        <div class="customers-table-header">
          <div>
            <span class="section-mini-label">Base de clientes</span>
            <h3>Lista de clientes</h3>
            <p>Visualize rapidamente os principais dados dos clientes cadastrados.</p>
          </div>
        </div>

        <div class="customers-toolbar">
          <div class="form-field">
            <label for="customerSearch">Buscar</label>
            <input
              id="customerSearch"
              [(ngModel)]="searchTerm"
              name="customerSearch"
              placeholder="Buscar por nome, CPF, telefone ou ID"
            >
          </div>

          <div class="form-field">
            <label for="customerFilter">Status</label>
            <select id="customerFilter" [(ngModel)]="customerFilter" name="customerFilter">
              <option value="all">Todos</option>
              <option value="complete">Cadastro completo</option>
              <option value="basic">Cadastro básico</option>
              <option value="inactive">Inativo</option>
            </select>
          </div>

          <div class="form-field">
            <label for="customerSort">Ordenar por</label>
            <select id="customerSort" [(ngModel)]="sortOption" name="customerSort">
              <option value="name">Nome</option>
              <option value="recent">Mais recente</option>
            </select>
          </div>
        </div>

        <div class="customers-results-bar">
          <span>{{ filteredCustomers.length }} cliente(s) encontrado(s)</span>
          <button type="button" class="ghost-btn ghost-btn-sm" (click)="clearFilters()">
            Limpar filtros
          </button>
        </div>

        <div class="table-responsive customers-table-responsive">
          <table>
            <thead>
              <tr>
                <th>Cliente</th>
                <th>CPF</th>
                <th>Telefone</th>
                <th>E-mail</th>
                <th>Endereço</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let customer of filteredCustomers">
                <td>
                  <div class="table-customer-cell">
                    <strong>{{ customer.name }}</strong>
                    <small>ID {{ customer.id || '-' }}</small>
                  </div>
                </td>
                <td>{{ customer.cpf || '-' }}</td>
                <td>{{ customer.phone || '-' }}</td>
                <td>{{ customer.email || '-' }}</td>
                <td>{{ customer.address || '-' }}</td>
                <td>
                  <span class="badge" [ngClass]="getCustomerBadgeClass(customer)">
                    {{ getCustomerStatusLabel(customer) }}
                  </span>
                </td>
              </tr>

              <tr *ngIf="!filteredCustomers.length">
                <td colspan="6">
                  <div class="empty-table-state">
                    <strong>Nenhum cliente encontrado com os filtros atuais.</strong>
                    <p>Ajuste a busca ou limpe os filtros para visualizar os clientes.</p>
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
export class CustomersPageComponent implements OnInit {
  customers: Customer[] = [];
  form: Customer = {
    name: '',
    cpf: '',
    phone: '',
    email: '',
    address: ''
  };

  searchTerm = '';
  customerFilter: CustomerFilter = 'all';
  sortOption: CustomerSortOption = 'name';

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getCustomers().subscribe(data => this.customers = data);
  }

  save(): void {
    this.clearFeedback();

    if (!this.form.name.trim()) {
      this.setError('Informe o nome do cliente.');
      return;
    }

    if (!this.form.phone.trim()) {
      this.setError('Informe o telefone do cliente.');
      return;
    }

    if (this.form.email?.trim() && !this.isValidEmail(this.form.email)) {
      this.setError('Informe um e-mail válido.');
      return;
    }

    this.api.createCustomer(this.form).subscribe({
      next: () => {
        this.form = {
          name: '',
          cpf: '',
          phone: '',
          email: '',
          address: ''
        };
        this.load();
        this.setSuccess('Cliente cadastrado com sucesso.');
      },
      error: () => {
        this.setError('Não foi possível salvar o cliente.');
      }
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.customerFilter = 'all';
    this.sortOption = 'name';
  }

  get customersWithPhone(): number {
    return this.customers.filter(customer => !!customer.phone?.trim()).length;
  }

  get customersWithEmail(): number {
    return this.customers.filter(customer => !!customer.email?.trim()).length;
  }

  get filteredCustomers(): Customer[] {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();

    const filtered = this.customers.filter(customer => {
      const matchesSearch =
        !normalizedSearch ||
        customer.name.toLowerCase().includes(normalizedSearch) ||
        (customer.cpf || '').toLowerCase().includes(normalizedSearch) ||
        (customer.phone || '').toLowerCase().includes(normalizedSearch) ||
        String(customer.id || '').includes(normalizedSearch);

      const matchesFilter =
        this.customerFilter === 'all' ||
        (this.customerFilter === 'inactive' && customer.active === false) ||
        (this.customerFilter === 'complete' && customer.active !== false && !!customer.email?.trim()) ||
        (this.customerFilter === 'basic' && customer.active !== false && !customer.email?.trim());

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

  getCustomerBadgeClass(customer: Customer): string {
    if (customer.active === false) {
      return 'critical';
    }

    if (customer.email?.trim()) {
      return 'ok';
    }

    return 'low';
  }

  getCustomerStatusLabel(customer: Customer): string {
    if (customer.active === false) {
      return 'Inativo';
    }

    if (customer.email?.trim()) {
      return 'Completo';
    }

    return 'Cadastro básico';
  }

  private isValidEmail(email: string): boolean {
    return /\S+@\S+\.\S+/.test(email.trim());
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