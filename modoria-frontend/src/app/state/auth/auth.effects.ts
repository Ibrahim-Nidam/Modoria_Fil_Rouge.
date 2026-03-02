import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { map, catchError, switchMap, tap } from 'rxjs/operators';
import * as AuthActions from './auth.actions';
import { AuthService } from '../../core/services/auth.service';

@Injectable()
export class AuthEffects {
    private actions$ = inject(Actions);
    private authService = inject(AuthService);
    private router = inject(Router);

    login$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.login),
            switchMap(({ email, password }) =>
                this.authService.login({ email, password }).pipe(
                    map(res => AuthActions.loginSuccess({
                        user: res.user,
                        accessToken: res.accessToken,
                        refreshToken: res.refreshToken,
                    })),
                    catchError(err => of(AuthActions.loginFailure({
                        error: err.error?.message || 'Login failed. Please try again.',
                    }))),
                ),
            ),
        ),
    );

    loginSuccess$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.loginSuccess),
            tap(({ user }) => {
                const isAdmin = user.roles.includes('ROLE_ADMIN');
                const isSupport = user.roles.includes('ROLE_SUPPORT');
                if (isAdmin || isSupport) {
                    this.router.navigate(['/admin/dashboard']);
                } else {
                    this.router.navigate(['/']);
                }
            }),
        ),
        { dispatch: false },
    );

    register$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.register),
            switchMap(({ firstName, lastName, email, password }) =>
                this.authService.register({ firstName, lastName, email, password }).pipe(
                    map(res => AuthActions.registerSuccess({
                        user: res.user,
                        accessToken: res.accessToken,
                        refreshToken: res.refreshToken,
                    })),
                    catchError(err => of(AuthActions.registerFailure({
                        error: err.error?.message || 'Registration failed. Please try again.',
                    }))),
                ),
            ),
        ),
    );

    registerSuccess$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.registerSuccess),
            tap(() => this.router.navigate(['/'])),
        ),
        { dispatch: false },
    );

    logout$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.logout),
            switchMap(() =>
                this.authService.logout().pipe(
                    map(() => AuthActions.logoutSuccess()),
                    catchError(() => of(AuthActions.logoutSuccess())),
                ),
            ),
        ),
    );

    logoutSuccess$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.logoutSuccess),
            tap(() => this.router.navigate(['/auth/login'])),
        ),
        { dispatch: false },
    );

    loadFromStorage$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.loadUserFromStorage),
            map(() => {
                const user = this.authService.getStoredUser();
                const accessToken = this.authService.getAccessToken();
                if (user && accessToken) {
                    return AuthActions.loadUserSuccess({ user, accessToken });
                }
                return AuthActions.logoutSuccess();
            }),
        ),
    );

    refreshToken$ = createEffect(() =>
        this.actions$.pipe(
            ofType(AuthActions.refreshToken),
            switchMap(() =>
                this.authService.refreshToken().pipe(
                    map(res => AuthActions.refreshTokenSuccess({ accessToken: res.accessToken })),
                    catchError(() => of(AuthActions.refreshTokenFailure())),
                ),
            ),
        ),
    );
}
