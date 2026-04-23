import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {
  HTTP_INTERCEPTORS,
  HttpClientModule
} from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { DashboardPageComponent } from './pages/dashboard/dashboard.component';
import { ProductsPageComponent } from './pages/products/products.component';
import { CustomersPageComponent } from './pages/customers/customers.component';
import { SalesPageComponent } from './pages/sales/sales.component';
import { LoginPageComponent } from './pages/login/login.component';
import { BlockedPageComponent } from './pages/blocked/blocked.component';

import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';

const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  { path: 'blocked', component: BlockedPageComponent },

  {
    path: '',
    component: DashboardPageComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'products',
    component: ProductsPageComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'customers',
    component: CustomersPageComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'sales',
    component: SalesPageComponent,
    canActivate: [AuthGuard]
  },

  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    DashboardPageComponent,
    ProductsPageComponent,
    CustomersPageComponent,
    SalesPageComponent,
    LoginPageComponent,
    BlockedPageComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    AuthGuard,
    RoleGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}