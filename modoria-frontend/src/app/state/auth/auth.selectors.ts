import { createSelector, createFeatureSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>('auth');

export const selectCurrentUser = createSelector(selectAuthState, s => s.user);
export const selectAccessToken = createSelector(selectAuthState, s => s.accessToken);
export const selectIsAuthenticated = createSelector(selectAuthState, s => s.isAuthenticated);
export const selectAuthLoading = createSelector(selectAuthState, s => s.loading);
export const selectAuthError = createSelector(selectAuthState, s => s.error);

export const selectIsAdmin = createSelector(
    selectCurrentUser,
    user => user?.roles?.includes('ROLE_ADMIN') ?? false,
);

export const selectIsSupport = createSelector(
    selectCurrentUser,
    user => user?.roles?.includes('ROLE_SUPPORT') ?? false,
);
