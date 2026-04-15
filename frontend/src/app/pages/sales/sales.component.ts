import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Product } from '../../models/product.model';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-sales-page',
  template: `
    <h2>Vendas</h2>
    <div class="grid grid-2">
      <div class="card">
        <h3>Novo item</h3>
        <select [(ngModel)]="selectedProductId" name="productId">
          <option [ngValue]="undefined">Selecione um produto</option>
          <option *ngFor="let product of products" [ngValue]="product.id">{{ product.name }} - estoque {{ product.stock }}</option>
        </select>
        <input [(ngModel)]="quantity" name="quantity" type="number" min="1" placeholder="Quantidade">
        <button (click)="addItem()">Adicionar ao carrinho</button>

        <h3 style="margin-top:16px;">Cliente</h3>
        <select [(ngModel)]="selectedCustomerId" name="customerId">
          <option [ngValue]="null">Cliente não identificado</option>
          <option *ngFor="let customer of customers" [ngValue]="customer.id">{{ customer.name }}</option>
        </select>
        <input [(ngModel)]="paymentMethod" name="paymentMethod" placeholder="Forma de pagamento">
        <input [(ngModel)]="discount" name="discount" type="number" min="0" placeholder="Desconto">
        <button (click)="finishSale()">Finalizar venda</button>
      </div>

      <div class="card">
        <h3>Carrinho</h3>
        <table>
          <thead><tr><th>Produto</th><th>Qtd</th><th>Subtotal</th></tr></thead>
          <tbody>
            <tr *ngFor="let item of cart">
              <td>{{ item.name }}</td>
              <td>{{ item.quantity }}</td>
              <td>R$ {{ item.subtotal | number:'1.2-2' }}</td>
            </tr>
          </tbody>
        </table>
        <p><strong>Total:</strong> R$ {{ total | number:'1.2-2' }}</p>
      </div>
    </div>
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
    this.api.getProducts().subscribe(data => this.products = data.filter(p => p.stock > 0));
    this.api.getCustomers().subscribe(data => this.customers = data);
  }

  get total(): number { return this.cart.reduce((sum, item) => sum + item.subtotal, 0) - Number(this.discount || 0); }

  addItem() {
    const product = this.products.find(p => p.id === this.selectedProductId);
    if (!product) return;
    this.cart.push({
      productId: product.id,
      name: product.name,
      quantity: this.quantity,
      subtotal: Number(product.salePrice) * Number(this.quantity)
    });
  }

  finishSale() {
    const payload = {
      customerId: this.selectedCustomerId,
      paymentMethod: this.paymentMethod,
      discount: this.discount,
      notes: '',
      items: this.cart.map(item => ({ productId: item.productId, quantity: item.quantity }))
    };
    this.api.createSale(payload).subscribe(() => {
      this.cart = [];
      this.discount = 0;
      this.api.getProducts().subscribe(data => this.products = data.filter(p => p.stock > 0));
      alert('Venda realizada com sucesso');
    });
  }
}
