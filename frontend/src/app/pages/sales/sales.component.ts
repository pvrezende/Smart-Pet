import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Product } from '../../models/product.model';
import { Customer } from '../../models/customer.model';

interface CartItem {
  productId: number;
  name: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  maxStock: number;
}

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

      <div *ngIf="feedbackMessage" class="card sales-feedback" [class.sales-feedback-error]="feedbackType === 'error'">
        <strong>{{ feedbackType === 'success' ? 'Sucesso:' : 'Atenção:' }}</strong>
        <span>{{ feedbackMessage }}</span>
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
                  {{ product.stock <= 0 ? ' - Sem estoque' : '' }}
                </option>
              </select>

              <small class="field-helper" *ngIf="selectedProduct">
                Selecionado: {{ selectedProduct.name }} | Estoque atual: {{ selectedProduct.stock }}
              </small>
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
                (blur)="normalizeDiscount()"
              >
            </div>
          </div>
        </article>

        <aside class="sales-summary-panel">
          <article class="summary-mini-card">
            <span class="summary-mini-label">Itens no carrinho</span>
            <strong class="summary-mini-value">{{ totalItems }}</strong>
          </article>

          <article class="summary-mini-card">
            <span class="summary-mini-label">Produtos disponíveis</span>
            <strong class="summary-mini-value">{{ availableProductsCount }}</strong>
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
                  <th>Unitário</th>
                  <th>Subtotal</th>
                  <th>Ações</th>
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
                  <td>R$ {{ item.unitPrice | number:'1.2-2' }}</td>
                  <td>R$ {{ item.subtotal | number:'1.2-2' }}</td>
                  <td>
                    <div class="inventory-actions">
                      <button
                        type="button"
                        class="table-action-btn danger"
                        (click)="removeItem(item.productId)"
                      >
                        Remover
                      </button>
                    </div>
                  </td>
                </tr>

                <tr *ngIf="!cart.length">
                  <td colspan="5">
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
              <strong>{{ totalItems }}</strong>
            </div>

            <div class="checkout-line">
              <span>Subtotal</span>
              <strong>R$ {{ subtotal | number:'1.2-2' }}</strong>
            </div>

            <div class="checkout-line">
              <span>Desconto aplicado</span>
              <strong>R$ {{ sanitizedDiscount | number:'1.2-2' }}</strong>
            </div>

            <div class="checkout-total-box">
              <span>Total final</span>
              <strong>R$ {{ total | number:'1.2-2' }}</strong>
            </div>

            <button
              type="button"
              class="primary-action-btn checkout-btn"
              (click)="finishSale()"
              [disabled]="!canFinishSale"
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
  cart: CartItem[] = [];
  selectedProductId?: number;
  selectedCustomerId: number | null = null;
  quantity = 1;
  paymentMethod = 'PIX';
  discount: number | string = 0;

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.reloadData();
  }

  reloadData(): void {
    this.api.getProducts().subscribe(data => {
      this.products = data;
    });

    this.api.getCustomers().subscribe(data => {
      this.customers = data;
    });
  }

  get totalItems(): number {
    return this.cart.reduce((sum, item) => sum + Number(item.quantity), 0);
  }

  get subtotal(): number {
    return this.cart.reduce((sum, item) => sum + Number(item.subtotal), 0);
  }

  get sanitizedDiscount(): number {
    const discountValue = Number(this.discount || 0);

    if (Number.isNaN(discountValue) || discountValue < 0) {
      return 0;
    }

    return discountValue > this.subtotal ? this.subtotal : discountValue;
  }

  get total(): number {
    return this.subtotal - this.sanitizedDiscount;
  }

  get availableProductsCount(): number {
    return this.products.filter(product => Number(product.stock) > 0).length;
  }

  get selectedProduct(): Product | undefined {
    return this.products.find(product => product.id === this.selectedProductId);
  }

  get canFinishSale(): boolean {
    return this.cart.length > 0 && this.total >= 0 && !!this.paymentMethod.trim();
  }

  normalizeDiscount(): void {
    this.discount = this.sanitizedDiscount;
  }

  addItem(): void {
    this.clearFeedback();

    const product = this.products.find(p => p.id === this.selectedProductId);

    if (!product) {
      this.setError('Selecione um produto antes de adicionar ao carrinho.');
      return;
    }

    if (Number(product.stock) <= 0) {
      this.setError(`"${product.name}" está sem estoque no momento.`);
      return;
    }

    const quantityToAdd = Number(this.quantity);

    if (Number.isNaN(quantityToAdd) || quantityToAdd <= 0) {
      this.setError('Informe uma quantidade válida maior que zero.');
      return;
    }

    const existingItem = this.cart.find(item => item.productId === product.id);
    const currentQuantityInCart = existingItem ? Number(existingItem.quantity) : 0;
    const requestedTotal = currentQuantityInCart + quantityToAdd;
    const availableStock = Number(product.stock);

    if (requestedTotal > availableStock) {
      this.setError(`Quantidade indisponível. Estoque atual de "${product.name}": ${availableStock}.`);
      return;
    }

    if (existingItem) {
      existingItem.quantity = requestedTotal;
      existingItem.subtotal = requestedTotal * Number(product.salePrice);
      this.setSuccess(`Quantidade de "${product.name}" atualizada no carrinho.`);
    } else {
      this.cart.push({
        productId: product.id!,
        name: product.name,
        quantity: quantityToAdd,
        unitPrice: Number(product.salePrice),
        subtotal: Number(product.salePrice) * quantityToAdd,
        maxStock: availableStock
      });
      this.setSuccess(`"${product.name}" adicionado ao carrinho.`);
    }

    this.quantity = 1;
    this.selectedProductId = undefined;
    this.normalizeDiscount();
  }

  removeItem(productId: number): void {
    const item = this.cart.find(cartItem => cartItem.productId === productId);
    this.cart = this.cart.filter(cartItem => cartItem.productId !== productId);

    if (item) {
      this.setSuccess(`"${item.name}" removido do carrinho.`);
    }

    this.normalizeDiscount();
  }

  finishSale(): void {
    this.clearFeedback();

    if (!this.cart.length) {
      this.setError('Adicione pelo menos um item ao carrinho antes de finalizar a venda.');
      return;
    }

    if (!this.paymentMethod.trim()) {
      this.setError('Informe a forma de pagamento.');
      return;
    }

    const payload: any = {
      paymentMethod: this.paymentMethod.trim(),
      discount: this.sanitizedDiscount,
      notes: '',
      items: this.cart.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    };

    if (this.selectedCustomerId !== null) {
      payload.customerId = this.selectedCustomerId;
    }

    this.api.createSale(payload).subscribe({
      next: () => {
        this.cart = [];
        this.discount = 0;
        this.selectedCustomerId = null;
        this.paymentMethod = 'PIX';
        this.quantity = 1;
        this.selectedProductId = undefined;
        this.reloadData();
        this.setSuccess('Venda realizada com sucesso.');
      },
      error: (error) => {
        console.error('Erro ao finalizar venda:', error);
        this.setError('Não foi possível finalizar a venda. Verifique os dados e tente novamente.');
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