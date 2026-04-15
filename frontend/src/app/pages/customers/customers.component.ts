import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Customer } from '../../models/customer.model';

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
              <tr *ngFor="let customer of customers">
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

              <tr *ngIf="!customers.length">
                <td colspan="6">
                  <div class="empty-table-state">
                    <strong>Nenhum cliente cadastrado ainda.</strong>
                    <p>Adicione o primeiro cliente usando o formulário acima.</p>
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

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getCustomers().subscribe(data => this.customers = data);
  }

  save(): void {
    this.api.createCustomer(this.form).subscribe(() => {
      this.form = {
        name: '',
        cpf: '',
        phone: '',
        email: '',
        address: ''
      };
      this.load();
    });
  }

  get customersWithPhone(): number {
    return this.customers.filter(customer => !!customer.phone?.trim()).length;
  }

  get customersWithEmail(): number {
    return this.customers.filter(customer => !!customer.email?.trim()).length;
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
}