import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Product } from '../../models/product.model';
import { Customer } from '../../models/customer.model';
import { Dashboard } from '../../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';
  constructor(private http: HttpClient) {}

  getDashboard(): Observable<Dashboard> { return this.http.get<Dashboard>(`${this.baseUrl}/dashboard`); }
  getProducts(): Observable<Product[]> { return this.http.get<Product[]>(`${this.baseUrl}/products`); }
  createProduct(payload: Product): Observable<Product> { return this.http.post<Product>(`${this.baseUrl}/products`, payload); }
  stockIn(id: number, quantity: number) { return this.http.post(`${this.baseUrl}/products/${id}/stock/in`, { quantity, observation: 'Entrada manual web' }); }
  stockOut(id: number, quantity: number) { return this.http.post(`${this.baseUrl}/products/${id}/stock/out`, { quantity, observation: 'Saída manual web' }); }

  getCustomers(): Observable<Customer[]> { return this.http.get<Customer[]>(`${this.baseUrl}/customers`); }
  createCustomer(payload: Customer): Observable<Customer> { return this.http.post<Customer>(`${this.baseUrl}/customers`, payload); }

  getSales() { return this.http.get<any[]>(`${this.baseUrl}/sales`); }
  createSale(payload: any) {
  return this.http.post(`${this.baseUrl}/sales`, payload, {
    responseType: 'text' as 'json'
  });
}
}
