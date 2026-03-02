import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { Button } from '../../../shared/ui/button/button';
import { Input } from '../../../shared/ui/input/input';
import * as AuthActions from '../../../state/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../../state/auth/auth.selectors';
import { Subject, takeUntil } from 'rxjs';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, AsyncPipe, Button, Input],
    templateUrl: './login.html',
    styleUrl: './login.css',
})
export class Login implements OnInit, OnDestroy {
    private fb = inject(FormBuilder);
    private store = inject(Store);
    private destroy$ = new Subject<void>();

    loading$ = this.store.select(selectAuthLoading);
    error$ = this.store.select(selectAuthError);

    form = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
    });

    ngOnInit(): void {
        this.store.dispatch(AuthActions.clearAuthError());
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    getError(field: string): string {
        const ctrl = this.form.get(field);
        if (!ctrl?.touched || ctrl.valid) return '';
        if (ctrl.hasError('required')) return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
        if (ctrl.hasError('email')) return 'Please enter a valid email address';
        if (ctrl.hasError('minlength')) return 'Password must be at least 6 characters';
        return '';
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const { email, password } = this.form.value;
        this.store.dispatch(AuthActions.login({ email: email!, password: password! }));
    }
}
