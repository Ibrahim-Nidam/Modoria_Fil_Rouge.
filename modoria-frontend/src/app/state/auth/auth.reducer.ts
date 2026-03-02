import { createReducer, on } from '@ngrx/store';
import { User } from '../../core/models/auth.model';
import * as AuthActions from './auth.actions';

export interface AuthState {
    user: User | null;
    accessToken: string | null;
    loading: boolean;
    error: string | null;
    isAuthenticated: boolean;
}

export const initialAuthState: AuthState = {
    user: null,
    accessToken: null,
    loading: false,
    error: null,
    isAuthenticated: false,
};

export const authReducer = createReducer(
    initialAuthState,

    // Login
    on(AuthActions.login, state => ({ ...state, loading: true, error: null })),
    on(AuthActions.loginSuccess, (state, { user, accessToken }) => ({
        ...state, user, accessToken, loading: false, error: null, isAuthenticated: true,
    })),
    on(AuthActions.loginFailure, (state, { error }) => ({
        ...state, loading: false, error, isAuthenticated: false,
    })),

    // Register
    on(AuthActions.register, state => ({ ...state, loading: true, error: null })),
    on(AuthActions.registerSuccess, (state, { user, accessToken }) => ({
        ...state, user, accessToken, loading: false, error: null, isAuthenticated: true,
    })),
    on(AuthActions.registerFailure, (state, { error }) => ({
        ...state, loading: false, error, isAuthenticated: false,
    })),

    // Logout
    on(AuthActions.logoutSuccess, () => initialAuthState),

    // Refresh
    on(AuthActions.refreshTokenSuccess, (state, { accessToken }) => ({ ...state, accessToken })),
    on(AuthActions.refreshTokenFailure, () => initialAuthState),

    // Load from storage
    on(AuthActions.loadUserSuccess, (state, { user, accessToken }) => ({
        ...state, user, accessToken, isAuthenticated: true,
    })),

    // Clear error
    on(AuthActions.clearAuthError, state => ({ ...state, error: null })),
);
