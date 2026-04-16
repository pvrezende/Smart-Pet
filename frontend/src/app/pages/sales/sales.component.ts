import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Product } from '../../models/product.model';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-sales-page',
  template: `
    <section class="sales-page">
      <div class="section-heading">
        <div>
          <h2>Gestão de vendas</h2>
          <p>Registre vendas, organize o carrinho e acompanhe o fechamento dos pedidos.</p>
        </div>
        <button type="button" class="ghost-btn" (click)="reloadData()">Atualizar dados</button>
      </div>

      <div class="sales-layout">
        <article class="sales-form-card">
          <div class="form-card-header">
            <span class="section-mini-label">Nova venda</span>
            <h3>Lançamento de itens</h3>
            <p>Selecione produtos, defina o cliente e finalize a venda com segurança.</p>
          </div>

          <div class="sales-form-grid">
            <div class="form-field form-field-full">
              <label for="productId">Produto</label>
              <select id="productId" [(ngModel)]="selectedProductId" name="productId">
                <option [ngValue]="undefined">Selecione um produto</option>
                <option *ngFor="let product of products" [ngValue]="product.id">
                  {{ product.name }} - estoque {{ product.stock }}
                </option>
              </select>
            </div>

            <div class="form-field">
              <label for="quantity">Quantidade</label>
              <input
                id="quantity"
                [(ngModel)]="quantity"
                name="quantity"
                type="number"
                min="1"
                placeholder="1"
              >
            </div>

            <div class="form-field sales-inline-action">
              <label>&nbsp;</label>
              <button type="button" class="primary-action-btn" (click)="addItem()">
                Adicionar ao carrinho
              </button>
            </div>

            <div class="form-field form-field-full">
              <label for="customerId">Cliente</label>
              <select id="customerId" [(ngModel)]="selectedCustomerId" name="customerId">
                <option [ngValue]="null">Cliente não identificado</option>
                <option *ngFor="let customer of customers" [ngValue]="customer.id">
                  {{ customer.name }}
                </option>
              </select>
            </div>

            <div class="form-field">
              <label for="paymentMethod">Forma de pagamento</label>
              <input
                id="paymentMethod"
                [(ngModel)]="paymentMethod"
                name="paymentMethod"
                placeholder="Ex.: PIX"
              >
            </div>

            <div class="form-field">
              <label for="discount">Desconto</label>
              <input
                id="discount"
                [(ngModel)]="discount"
                name="discount"
                type="number"
                min="0"
                step="0.01"
                placeholder="0,00"
              >
            </div>
          </div>
        </article>

        <aside class="sales-summary-panel">
          <article class="summary-mini-card">
            <span class="summary-mini-label">Itens no carrinho</span>
            <strong class="summary-mini-value">{{ cart.length }}</strong>
          </article>

          <article class="summary-mini-card">
            <span class="summary-mini-label">Produtos disponíveis</span>
            <strong class="summary-mini-value">{{ products.length }}</strong>
          </article>

          <article class="summary-mini-card summary-mini-card-highlight">
            <span class="summary-mini-label">Total da venda</span>
            <strong class="summary-mini-value">R$ {{ total | number:'1.2-2' }}</strong>
          </article>
        </aside>
      </div>

      <article class="sales-cart-card">
        <div class="sales-cart-header">
          <div>
            <span class="section-mini-label">Carrinho</span>
            <h3>Resumo da venda</h3>
            <p>Confira os itens adicionados antes de concluir o lançamento.</p>
          </div>
        </div>

        <div class="sales-cart-grid">
          <div class="table-responsive sales-table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Qtd</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of cart">
                  <td>
                    <div class="table-sale-cell">
                      <strong>{{ item.name }}</strong>
                      <small>ID {{ item.productId }}</small>
                    </div>
                  </td>
                  <td>{{ item.quantity }}</td>
                  <td>R$ {{ item.subtotal | number:'1.2-2' }}</td>
                </tr>

                <tr *ngIf="!cart.length">
                  <td colspan="3">
                    <div class="empty-table-state">
                      <strong>Seu carrinho está vazio.</strong>
                      <p>Selecione um produto acima para começar a venda.</p>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="sale-checkout-panel">
            <div class="checkout-line">
              <span>Itens adicionados</span>
              <strong>{{ cart.length }}</strong>
            </div>

            <div class="checkout-line">
              <span>Desconto aplicado</span>
              <strong>R$ {{ discount | number:'1.2-2' }}</strong>
            </div>

            <div class="checkout-total-box">
              <span>Total final</span>
              <strong>R$ {{ total | number:'1.2-2' }}</strong>
            </div>

            <button
              type="button"
              class="primary-action-btn checkout-btn"
              (click)="finishSale()"
              [disabled]="!cart.length"
            >
              Finalizar venda
            </button>
          </div>
        </div>
      </article>
    </section>
  `
})
export class SalesPageComponent implements OnInit {
  products: Product[] = [];
  customers: Customer[] = [];
  cart: any[] = [];
  selectedProductId?: number;
  selectedCustomerId: number | null = null;
  quantity = 1;
  paymentMethod = 'PIX';
  discount = 0;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.reloadData();
  }

  reloadData(): void {
    this.api.getProducts().subscribe(data => this.products = data.filter(p => p.stock > 0));
    this.api.getCustomers().subscribe(data => this.customers = data);
  }

  get total(): number {
    return this.cart.reduce((sum, item) => sum + item.subtotal, 0) - Number(this.discount || 0);
  }

  addItem(): void {
    const product = this.products.find(p => p.id === this.selectedProductId);
    if (!product) return;

    this.cart.push({
      productId: product.id,
      name: product.name,
      quantity: this.quantity,
      subtotal: Number(product.salePrice) * Number(this.quantity)
    });

    this.quantity = 1;
    this.selectedProductId = undefined;
  }

  finishSale(): void {
    const payload = {
      customerId: this.selectedCustomerId,
      paymentMethod: this.paymentMethod,
      discount: this.discount,
      notes: '',
      items: this.cart.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    };

    this.api.createSale(payload).subscribe(() => {
      this.cart = [];
      this.discount = 0;
      this.selectedCustomerId = null;
      this.paymentMethod = 'PIX';
      this.reloadData();
      alert('Venda realizada com sucesso');
    });
  }
}