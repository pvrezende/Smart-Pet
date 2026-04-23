import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { SaasBlockState } from '../../models/auth.models';

@Injectable({ providedIn: 'root' })
export class SaasBlockService {
  private readonly initialState: SaasBlockState = {
    blocked: false,
    title: '',
    description: '',
    backendMessage: ''
  };

  private readonly stateSubject = new BehaviorSubject<SaasBlockState>(this.initialState);
  state$ = this.stateSubject.asObservable();

  get snapshot(): SaasBlockState {
    return this.stateSubject.value;
  }

  setBlocked(message: string): void {
    const normalized = (message || '').toLowerCase();

    if (normalized.includes('cobrança em atraso')) {
      this.stateSubject.next({
        blocked: true,
        title: 'Cobrança em atraso',
        description: 'Sua loja está com cobrança em atraso. Regularize o pagamento para voltar a usar o sistema.',
        backendMessage: message
      });
      return;
    }

    if (normalized.includes('suspensa')) {
      this.stateSubject.next({
        blocked: true,
        title: 'Assinatura suspensa',
        description: 'Sua assinatura está suspensa. Regularize a situação para voltar a usar o sistema.',
        backendMessage: message
      });
      return;
    }

    if (normalized.includes('cancelada')) {
      this.stateSubject.next({
        blocked: true,
        title: 'Assinatura cancelada',
        description: 'Sua assinatura foi cancelada. Reative um plano para voltar a usar o sistema.',
        backendMessage: message
      });
      return;
    }

    if (normalized.includes('trial')) {
      this.stateSubject.next({
        blocked: true,
        title: 'Período de teste expirado',
        description: 'O período de trial da loja expirou. Regularize a assinatura para continuar usando o sistema.',
        backendMessage: message
      });
      return;
    }

    this.stateSubject.next({
      blocked: true,
      title: 'Acesso bloqueado',
      description: 'O acesso da loja foi bloqueado temporariamente. Regularize a situação para continuar.',
      backendMessage: message
    });
  }

  clear(): void {
    this.stateSubject.next(this.initialState);
  }
}