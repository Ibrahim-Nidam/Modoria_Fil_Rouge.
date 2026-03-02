import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Button } from '../../../shared/ui/button/button';
import { Input } from '../../../shared/ui/input/input';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, Button, Input],
    templateUrl: './forgot-password.html',
    styleUrl: './forgot-password.css',
})
export class ForgotPassword {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);

    loading = signal(false);
    success = signal(false);
    error = signal('');

    form = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
    });

    getEmailError(): string {
        const ctrl = this.form.get('email');
        if (!ctrl?.touched || ctrl.valid) return '';
        if (ctrl.hasError('required')) return 'Email is required';
        if (ctrl.hasError('email')) return 'Enter a valid email address';
        return '';
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.loading.set(true);
        this.error.set('');
        const { email } = this.form.value;
        this.authService.forgotPassword({ email: email! }).subscribe({
            next: () => { this.loading.set(false); this.success.set(true); },
            error: err => {
                this.loading.set(false);
                this.error.set(err.error?.message || 'Failed to send reset email.');
            },
        });
    }
}
