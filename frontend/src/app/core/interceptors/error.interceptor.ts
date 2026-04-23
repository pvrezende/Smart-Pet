import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { SaasBlockService } from '../services/saas-block.service';
import { ApiErrorResponse } from '../../models/auth.models';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private router: Router,
    private auth: AuthService,
    private saasBlockService: SaasBlockService
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse | undefined;
        const message = apiError?.message || '';

        if (error.status === 401) {
          this.auth.logout();
          this.router.navigate(['/login']);
        }

        if (error.status === 403 && this.isSaasBlockedMessage(message)) {
          this.saasBlockService.setBlocked(message);
          this.router.navigate(['/blocked']);
        }

        return throwError(() => error);
      })
    );
  }

  private isSaasBlockedMessage(message: string): boolean {
    const normalized = (message || '').toLowerCase();

    return (
      normalized.includes('cobrança em atraso') ||
      normalized.includes('assinatura da loja está suspensa') ||
      normalized.includes('assinatura da loja está cancelada') ||
      normalized.includes('trial')
    );
  }
}