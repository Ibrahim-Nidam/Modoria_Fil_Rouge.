import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { Button } from '../../../shared/ui/button/button';
import { Input } from '../../../shared/ui/input/input';
import * as AuthActions from '../../../state/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../../state/auth/auth.selectors';

function matchPasswords(group: import('@angular/forms').AbstractControl) {
    const pass = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass === confirm ? null : { mismatch: true };
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, AsyncPipe, Button, Input],
    templateUrl: './register.html',
    styleUrl: './register.css',
})
export class Register implements OnInit, OnDestroy {
    private fb = inject(FormBuilder);
    private store = inject(Store);

    loading$ = this.store.select(selectAuthLoading);
    error$ = this.store.select(selectAuthError);

    form = this.fb.group({
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
    }, { validators: matchPasswords });

    ngOnInit(): void {
        this.store.dispatch(AuthActions.clearAuthError());
    }

    ngOnDestroy(): void { }

    getError(field: string): string {
        const ctrl = this.form.get(field);
        if (!ctrl?.touched || ctrl.valid) return '';
        if (ctrl.hasError('required')) return 'This field is required';
        if (ctrl.hasError('email')) return 'Please enter a valid email';
        if (ctrl.hasError('minlength')) {
            const min = ctrl.errors?.['minlength']?.requiredLength;
            return `Must be at least ${min} characters`;
        }
        return '';
    }

    getConfirmError(): string {
        const ctrl = this.form.get('confirmPassword');
        if (!ctrl?.touched) return '';
        if (this.form.hasError('mismatch')) return 'Passwords do not match';
        return '';
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        const { firstName, lastName, email, password } = this.form.value;
        this.store.dispatch(AuthActions.register({
            firstName: firstName!,
            lastName: lastName!,
            email: email!,
            password: password!,
        }));
    }
}
