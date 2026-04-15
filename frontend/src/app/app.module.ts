import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { DashboardPageComponent } from './pages/dashboard/dashboard.component';
import { ProductsPageComponent } from './pages/products/products.component';
import { CustomersPageComponent } from './pages/customers/customers.component';
import { SalesPageComponent } from './pages/sales/sales.component';

const routes: Routes = [
  { path: '', component: DashboardPageComponent },
  { path: 'products', component: ProductsPageComponent },
  { path: 'customers', component: CustomersPageComponent },
  { path: 'sales', component: SalesPageComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    DashboardPageComponent,
    ProductsPageComponent,
    CustomersPageComponent,
    SalesPageComponent
  ],
  imports: [BrowserModule, FormsModule, HttpClientModule, RouterModule.forRoot(routes)],
  bootstrap: [AppComponent]
})
export class AppModule {}
