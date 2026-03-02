import { Routes } from '@angular/router';
import { noAuthGuard } from '../../core/guards/auth.guard';

export const authRoutes: Routes = [
    {
        path: '',
        loadComponent: () => import('./components/auth-layout/auth-layout').then(m => m.AuthLayout),
        children: [
            { path: '', redirectTo: 'login', pathMatch: 'full' },
            {
                path: 'login',
                canActivate: [noAuthGuard],
                loadComponent: () => import('./login/login').then(m => m.Login),
            },
            {
                path: 'register',
                canActivate: [noAuthGuard],
                loadComponent: () => import('./register/register').then(m => m.Register),
            },
            {
                path: 'forgot-password',
                canActivate: [noAuthGuard],
                loadComponent: () => import('./forgot-password/forgot-password').then(m => m.ForgotPassword),
            },
            {
                path: 'reset-password',
                canActivate: [noAuthGuard],
                loadComponent: () => import('./reset-password/reset-password').then(m => m.ResetPassword),
            },
        ],
    },
];
