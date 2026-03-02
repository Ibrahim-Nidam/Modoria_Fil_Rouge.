import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    LoginRequest, RegisterRequest, AuthResponse,
    ForgotPasswordRequest, ResetPasswordRequest
} from '../models/auth.model';

const AUTH_BASE = '/api/v1/auth';
const TOKEN_KEY = 'modoria_access_token';
const REFRESH_KEY = 'modoria_refresh_token';
const USER_KEY = 'modoria_user';

// Backend wraps all responses in ApiResponse<T>
interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);

    login(payload: LoginRequest): Observable<AuthResponse> {
        return this.http.post<ApiResponse<AuthResponse>>(`${AUTH_BASE}/login`, payload).pipe(
            map(r => r.data),
            tap(res => this.persistSession(res)),
        );
    }

    register(payload: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<ApiResponse<AuthResponse>>(`${AUTH_BASE}/register`, payload).pipe(
            map(r => r.data),
            tap(res => this.persistSession(res)),
        );
    }

    logout(): Observable<void> {
        return this.http.post<ApiResponse<void>>(`${AUTH_BASE}/logout`, {}).pipe(
            map(r => r.data),
            tap(() => this.clearSession()),
        );
    }

    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.getRefreshToken();
        return this.http.post<ApiResponse<AuthResponse>>(`${AUTH_BASE}/refresh`, { refreshToken }).pipe(
            map(r => r.data),
            tap(res => this.persistSession(res)),
        );
    }

    forgotPassword(payload: ForgotPasswordRequest): Observable<void> {
        return this.http.post<void>(`${AUTH_BASE}/forgot-password`, payload);
    }

    resetPassword(payload: ResetPasswordRequest): Observable<void> {
        return this.http.post<void>(`${AUTH_BASE}/reset-password`, payload);
    }

    persistSession(res: AuthResponse): void {
        if (typeof localStorage === 'undefined') return;
        localStorage.setItem(TOKEN_KEY, res.accessToken);
        localStorage.setItem(REFRESH_KEY, res.refreshToken);
        localStorage.setItem(USER_KEY, JSON.stringify(res.user));
    }

    clearSession(): void {
        if (typeof localStorage === 'undefined') return;
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_KEY);
        localStorage.removeItem(USER_KEY);
    }

    getAccessToken(): string | null {
        if (typeof localStorage === 'undefined') return null;
        return localStorage.getItem(TOKEN_KEY);
    }

    getRefreshToken(): string | null {
        if (typeof localStorage === 'undefined') return null;
        return localStorage.getItem(REFRESH_KEY);
    }

    getStoredUser() {
        if (typeof localStorage === 'undefined') return null;
        const raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    }

    isLoggedIn(): boolean {
        return !!this.getAccessToken();
    }
}
