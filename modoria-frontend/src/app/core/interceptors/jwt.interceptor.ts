import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { selectAccessToken } from '../../state/auth/auth.selectors';
import * as AuthActions from '../../state/auth/auth.actions';
import { take } from 'rxjs/operators';

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
    const authService = inject(AuthService);
    const store = inject(Store);

    const token = authService.getAccessToken();

    const authReq = token
        ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : req;

    return next(authReq).pipe(
        catchError((err: HttpErrorResponse) => {
            const isAuthReq = req.url.includes('/auth/');
            const isRetry = req.headers.has('x-retry-count');

            if (err.status === 401 && !isAuthReq && !isRetry) {
                store.dispatch(AuthActions.refreshToken());
                return store.select(selectAccessToken).pipe(
                    take(1),
                    switchMap(newToken => {
                        if (!newToken) {
                            store.dispatch(AuthActions.logout());
                            return throwError(() => err);
                        }
                        const retried = req.clone({
                            setHeaders: {
                                Authorization: `Bearer ${newToken}`,
                                'x-retry-count': '1'
                            }
                        });
                        return next(retried);
                    }),
                );
            }
            return throwError(() => err);
        }),
    );
};
