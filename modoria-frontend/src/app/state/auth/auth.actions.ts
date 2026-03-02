import { createAction, props } from '@ngrx/store';
import { User } from '../../core/models/auth.model';

export const login = createAction('[Auth] Login', props<{ email: string; password: string }>());
export const loginSuccess = createAction('[Auth] Login Success', props<{ user: User; accessToken: string; refreshToken: string }>());
export const loginFailure = createAction('[Auth] Login Failure', props<{ error: string }>());

export const register = createAction('[Auth] Register', props<{ firstName: string; lastName: string; email: string; password: string }>());
export const registerSuccess = createAction('[Auth] Register Success', props<{ user: User; accessToken: string; refreshToken: string }>());
export const registerFailure = createAction('[Auth] Register Failure', props<{ error: string }>());

export const logout = createAction('[Auth] Logout');
export const logoutSuccess = createAction('[Auth] Logout Success');

export const refreshToken = createAction('[Auth] Refresh Token');
export const refreshTokenSuccess = createAction('[Auth] Refresh Token Success', props<{ accessToken: string }>());
export const refreshTokenFailure = createAction('[Auth] Refresh Token Failure');

export const loadUserFromStorage = createAction('[Auth] Load User From Storage');
export const loadUserSuccess = createAction('[Auth] Load User Success', props<{ user: User; accessToken: string }>());
export const clearAuthError = createAction('[Auth] Clear Error');
