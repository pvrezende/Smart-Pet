import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-products-page',
  template: `
    <section class="products-page">
      <div class="section-heading">
        <div>
          <h2>Gestão de produtos</h2>
          <p>Cadastre, acompanhe e ajuste o estoque dos produtos da loja.</p>
        </div>
        <button type="button" class="ghost-btn" (click)="load()">Atualizar lista</button>
      </div>

      <div class="products-layout">
        <article class="product-form-card">
          <div class="form-card-header">
            <span class="section-mini-label">Cadastro</span>
            <h3>Novo produto</h3>
            <p>Preencha os dados principais para adicionar um novo item ao sistema.</p>
          </div>

          <form class="product-form-grid" (ngSubmit)="save()">
            <div class="form-field form-field-full">
              <label for="name">Nome do produto</label>
              <input
                id="name"
                [(ngModel)]="form.name"
                name="name"
                placeholder="Ex.: Ração Premium Filhotes"
                required
              >
            </div>

            <div class="form-field">
              <label for="brand">Marca</label>
              <input
                id="brand"
                [(ngModel)]="form.brand"
                name="brand"
                placeholder="Ex.: Royal Canin"
                required
              >
            </div>

            <div class="form-field">
              <label for="animalType">Tipo de animal</label>
              <select id="animalType" [(ngModel)]="form.animalType" name="animalType">
                <option value="cao">Cão</option>
                <option value="gato">Gato</option>
              </select>
            </div>

            <div class="form-field">
              <label for="weight">Peso</label>
              <input
                id="weight"
                [(ngModel)]="form.weight"
                name="weight"
                type="number"
                min="0"
                step="0.01"
                placeholder="0,00"
              >
            </div>

            <div class="form-field">
              <label for="costPrice">Preço de custo</label>
              <input
                id="costPrice"
                [(ngModel)]="form.costPrice"
                name="costPrice"
                type="number"
                min="0"
                step="0.01"
                placeholder="0,00"
              >
            </div>

            <div class="form-field">
              <label for="salePrice">Preço de venda</label>
              <input
                id="salePrice"
                [(ngModel)]="form.salePrice"
                name="salePrice"
                type="number"
                min="0"
                step="0.01"
                placeholder="0,00"
              >
            </div>

            <div class="form-field">
              <label for="stock">Estoque inicial</label>
              <input
                id="stock"
                [(ngModel)]="form.stock"
                name="stock"
                type="number"
                min="0"
                placeholder="0"
              >
            </div>

            <div class="form-field">
              <label for="minimumStock">Estoque mínimo</label>
              <input
                id="minimumStock"
                [(ngModel)]="form.minimumStock"
                name="minimumStock"
                type="number"
                min="0"
                placeholder="0"
              >
            </div>

            <div class="form-actions form-field-full">
              <button type="submit" class="primary-action-btn">Salvar produto</button>
            </div>
          </form>
        </article>

        <aside class="product-summary-panel">
          <article class="summary-mini-card">
            <span class="summary-mini-label">Produtos cadastrados</span>
            <strong class="summary-mini-value">{{ products.length }}</strong>
          </article>

          <article class="summary-mini-card">
            <span class="summary-mini-label">Itens com estoque baixo</span>
            <strong class="summary-mini-value">{{ lowStockCount }}</strong>
          </article>

          <article class="summary-mini-card summary-mini-card-highlight">
            <span class="summary-mini-label">Valor potencial de venda</span>
            <strong class="summary-mini-value">
              R$ {{ totalSaleValue | number:'1.2-2' }}
            </strong>
          </article>
        </aside>
      </div>

      <article class="inventory-table-card">
        <div class="inventory-table-header">
          <div>
            <span class="section-mini-label">Estoque</span>
            <h3>Lista de produtos</h3>
            <p>Visão geral dos itens cadastrados com ações rápidas de movimentação.</p>
          </div>
        </div>

        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th>Produto</th>
                <th>Tipo</th>
                <th>Marca</th>
                <th>Peso</th>
                <th>Venda</th>
                <th>Estoque</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let product of products">
                <td>
                  <div class="table-product-cell">
                    <strong>{{ product.name }}</strong>
                    <small>ID {{ product.id || '-' }}</small>
                  </div>
                </td>
                <td>
                  <span class="table-tag">
                    {{ product.animalType === 'cao' ? 'Cão' : 'Gato' }}
                  </span>
                </td>
                <td>{{ product.brand }}</td>
                <td>{{ product.weight | number:'1.0-2' }} kg</td>
                <td>R$ {{ product.salePrice | number:'1.2-2' }}</td>
                <td>{{ product.stock }}</td>
                <td>
                  <span
                    class="badge"
                    [ngClass]="getStockBadgeClass(product)"
                  >
                    {{ getStockStatusLabel(product) }}
                  </span>
                </td>
                <td>
                  <div class="inventory-actions">
                    <button type="button" class="table-action-btn" (click)="stockIn(product)">
                      +5
                    </button>
                    <button
                      type="button"
                      class="table-action-btn secondary"
                      (click)="stockOut(product)"
                    >
                      -1
                    </button>
                  </div>
                </td>
              </tr>

              <tr *ngIf="!products.length">
                <td colspan="8">
                  <div class="empty-table-state">
                    <strong>Nenhum produto cadastrado ainda.</strong>
                    <p>Adicione o primeiro item usando o formulário acima.</p>
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
export class ProductsPageComponent implements OnInit {
  products: Product[] = [];
  form: Product = {
    name: '',
    animalType: 'cao',
    brand: '',
    weight: 1,
    costPrice: 0,
    salePrice: 0,
    stock: 0,
    minimumStock: 5
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.getProducts().subscribe(data => this.products = data);
  }

  save(): void {
    this.api.createProduct(this.form).subscribe(() => {
      this.form = {
        name: '',
        animalType: 'cao',
        brand: '',
        weight: 1,
        costPrice: 0,
        salePrice: 0,
        stock: 0,
        minimumStock: 5
      };
      this.load();
    });
  }

  stockIn(product: Product): void {
    this.api.stockIn(product.id!, 5).subscribe(() => this.load());
  }

  stockOut(product: Product): void {
    this.api.stockOut(product.id!, 1).subscribe(() => this.load());
  }

  get lowStockCount(): number {
    return this.products.filter(product => Number(product.stock) <= Number(product.minimumStock)).length;
  }

  get totalSaleValue(): number {
    return this.products.reduce(
      (total, product) => total + (Number(product.salePrice) * Number(product.stock)),
      0
    );
  }

  getStockBadgeClass(product: Product): string {
    if (Number(product.stock) <= 0) {
      return 'critical';
    }

    if (Number(product.stock) <= Number(product.minimumStock)) {
      return 'low';
    }

    return 'ok';
  }

  getStockStatusLabel(product: Product): string {
    if (Number(product.stock) <= 0) {
      return 'Sem estoque';
    }

    if (Number(product.stock) <= Number(product.minimumStock)) {
      return 'Estoque baixo';
    }

    return 'Estoque ok';
  }
}