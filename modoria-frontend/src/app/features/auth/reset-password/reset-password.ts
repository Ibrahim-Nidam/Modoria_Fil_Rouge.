import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { Button } from '../../../shared/ui/button/button';
import { Input } from '../../../shared/ui/input/input';

function matchPasswords(group: import('@angular/forms').AbstractControl) {
    const pass = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pass === confirm ? null : { mismatch: true };
}

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, Button, Input],
    templateUrl: './reset-password.html',
    styleUrl: './reset-password.css',
})
export class ResetPassword {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    loading = signal(false);
    success = signal(false);
    error = signal('');

    private token = this.route.snapshot.queryParamMap.get('token') ?? '';

    form = this.fb.group({
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
    }, { validators: matchPasswords });

    getError(field: string): string {
        const ctrl = this.form.get(field);
        if (!ctrl?.touched || ctrl.valid) return '';
        if (ctrl.hasError('required')) return 'This field is required';
        if (ctrl.hasError('minlength')) return 'Password must be at least 8 characters';
        if (field === 'confirmPassword' && this.form.hasError('mismatch')) return 'Passwords do not match';
        return '';
    }

    onSubmit(): void {
        if (this.form.invalid) { this.form.markAllAsTouched(); return; }
        this.loading.set(true);
        this.error.set('');
        const { newPassword } = this.form.value;
        this.authService.resetPassword({ token: this.token, newPassword: newPassword! }).subscribe({
            next: () => { this.loading.set(false); this.success.set(true); setTimeout(() => this.router.navigate(['/auth/login']), 2500); },
            error: err => { this.loading.set(false); this.error.set(err.error?.message || 'Reset failed. The link may have expired.'); },
        });
    }
}
