import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../models/auth.models';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const expectedRoles = (route.data['roles'] || []) as UserRole[];

    if (!expectedRoles.length) {
      return true;
    }

    if (this.auth.hasAnyRole(expectedRoles)) {
      return true;
    }

    return this.router.createUrlTree(['/']);
  }
}