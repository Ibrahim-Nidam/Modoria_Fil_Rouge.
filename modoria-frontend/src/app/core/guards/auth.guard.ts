import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs/operators';
import { selectIsAuthenticated } from '../../state/auth/auth.selectors';

export const authGuard: CanActivateFn = () => {
    const store = inject(Store);
    const router = inject(Router);

    return store.select(selectIsAuthenticated).pipe(
        take(1),
        map(isAuth => isAuth || router.createUrlTree(['/auth/login'])),
    );
};

export const noAuthGuard: CanActivateFn = () => {
    const store = inject(Store);
    const router = inject(Router);

    return store.select(selectIsAuthenticated).pipe(
        take(1),
        map(isAuth => !isAuth || router.createUrlTree(['/'])),
    );
};

export const adminGuard: CanActivateFn = () => {
    const store = inject(Store);
    const router = inject(Router);

    return store.select(selectIsAuthenticated).pipe(
        take(1),
        map(isAuth => isAuth || router.createUrlTree(['/auth/login'])),
    );
};
