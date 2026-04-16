import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Product } from '../../models/product.model';

type StockFilter = 'all' | 'low' | 'out' | 'ok';
type SortOption = 'name' | 'priceAsc' | 'priceDesc' | 'stockAsc' | 'stockDesc';

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

      <div *ngIf="feedbackMessage" class="card products-feedback" [class.products-feedback-error]="feedbackType === 'error'">
        <strong>{{ feedbackType === 'success' ? 'Sucesso:' : 'Atenção:' }}</strong>
        <span>{{ feedbackMessage }}</span>
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

        <div class="products-toolbar">
          <div class="form-field">
            <label for="searchTerm">Buscar</label>
            <input
              id="searchTerm"
              [(ngModel)]="searchTerm"
              name="searchTerm"
              placeholder="Buscar por nome, marca ou ID"
            >
          </div>

          <div class="form-field">
            <label for="animalFilter">Tipo</label>
            <select id="animalFilter" [(ngModel)]="animalFilter" name="animalFilter">
              <option value="all">Todos</option>
              <option value="cao">Cão</option>
              <option value="gato">Gato</option>
            </select>
          </div>

          <div class="form-field">
            <label for="stockFilter">Estoque</label>
            <select id="stockFilter" [(ngModel)]="stockFilter" name="stockFilter">
              <option value="all">Todos</option>
              <option value="ok">Estoque ok</option>
              <option value="low">Estoque baixo</option>
              <option value="out">Sem estoque</option>
            </select>
          </div>

          <div class="form-field">
            <label for="sortOption">Ordenar por</label>
            <select id="sortOption" [(ngModel)]="sortOption" name="sortOption">
              <option value="name">Nome</option>
              <option value="priceAsc">Preço crescente</option>
              <option value="priceDesc">Preço decrescente</option>
              <option value="stockAsc">Estoque crescente</option>
              <option value="stockDesc">Estoque decrescente</option>
            </select>
          </div>
        </div>

        <div class="products-results-bar">
          <span>{{ filteredProducts.length }} produto(s) encontrado(s)</span>
          <button type="button" class="ghost-btn ghost-btn-sm" (click)="clearFilters()">
            Limpar filtros
          </button>
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
              <tr *ngFor="let product of filteredProducts">
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
                  <span class="badge" [ngClass]="getStockBadgeClass(product)">
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
                      [disabled]="product.stock <= 0"
                    >
                      -1
                    </button>
                  </div>
                </td>
              </tr>

              <tr *ngIf="!filteredProducts.length">
                <td colspan="8">
                  <div class="empty-table-state">
                    <strong>Nenhum produto encontrado com os filtros atuais.</strong>
                    <p>Ajuste a busca ou limpe os filtros para visualizar os itens.</p>
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

  searchTerm = '';
  animalFilter: 'all' | 'cao' | 'gato' = 'all';
  stockFilter: StockFilter = 'all';
  sortOption: SortOption = 'name';

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const stockFilterParam = params['stockFilter'];

      if (
        stockFilterParam === 'all' ||
        stockFilterParam === 'low' ||
        stockFilterParam === 'out' ||
        stockFilterParam === 'ok'
      ) {
        this.stockFilter = stockFilterParam;
      }
    });

    this.load();
  }

  load(): void {
    this.api.getProducts().subscribe(data => this.products = data);
  }

  save(): void {
    this.clearFeedback();

    if (!this.form.name.trim()) {
      this.setError('Informe o nome do produto.');
      return;
    }

    if (!this.form.brand.trim()) {
      this.setError('Informe a marca do produto.');
      return;
    }

    if (Number(this.form.salePrice) < Number(this.form.costPrice)) {
      this.setError('O preço de venda não pode ser menor que o preço de custo.');
      return;
    }

    this.api.createProduct(this.form).subscribe({
      next: () => {
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
        this.setSuccess('Produto cadastrado com sucesso.');
      },
      error: () => {
        this.setError('Não foi possível salvar o produto.');
      }
    });
  }

  stockIn(product: Product): void {
    this.clearFeedback();
    this.api.stockIn(product.id!, 5).subscribe({
      next: () => {
        this.load();
        this.setSuccess(`Estoque de "${product.name}" atualizado com +5 unidades.`);
      },
      error: () => {
        this.setError(`Não foi possível adicionar estoque ao produto "${product.name}".`);
      }
    });
  }

  stockOut(product: Product): void {
    this.clearFeedback();

    if (Number(product.stock) <= 0) {
      this.setError(`"${product.name}" já está sem estoque.`);
      return;
    }

    this.api.stockOut(product.id!, 1).subscribe({
      next: () => {
        this.load();
        this.setSuccess(`Estoque de "${product.name}" atualizado com -1 unidade.`);
      },
      error: () => {
        this.setError(`Não foi possível retirar estoque do produto "${product.name}".`);
      }
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.animalFilter = 'all';
    this.stockFilter = 'all';
    this.sortOption = 'name';
  }

  get filteredProducts(): Product[] {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();

    const filtered = this.products.filter(product => {
      const matchesSearch =
        !normalizedSearch ||
        product.name.toLowerCase().includes(normalizedSearch) ||
        product.brand.toLowerCase().includes(normalizedSearch) ||
        String(product.id || '').includes(normalizedSearch);

      const matchesAnimal =
        this.animalFilter === 'all' || product.animalType === this.animalFilter;

      const matchesStock =
        this.stockFilter === 'all' ||
        (this.stockFilter === 'out' && Number(product.stock) <= 0) ||
        (
          this.stockFilter === 'low' &&
          Number(product.stock) > 0 &&
          Number(product.stock) <= Number(product.minimumStock)
        ) ||
        (
          this.stockFilter === 'ok' &&
          Number(product.stock) > Number(product.minimumStock)
        );

      return matchesSearch && matchesAnimal && matchesStock;
    });

    return filtered.sort((a, b) => {
      switch (this.sortOption) {
        case 'priceAsc':
          return Number(a.salePrice) - Number(b.salePrice);
        case 'priceDesc':
          return Number(b.salePrice) - Number(a.salePrice);
        case 'stockAsc':
          return Number(a.stock) - Number(b.stock);
        case 'stockDesc':
          return Number(b.stock) - Number(a.stock);
        case 'name':
        default:
          return a.name.localeCompare(b.name);
      }
    });
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