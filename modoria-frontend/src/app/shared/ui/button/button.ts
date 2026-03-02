import { Component, input, output } from '@angular/core';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
    selector: 'ui-button',
    standalone: true,
    templateUrl: './button.html',
    styleUrl: './button.css',
})
export class Button {
    label = input<string>('');
    variant = input<ButtonVariant>('primary');
    size = input<ButtonSize>('md');
    type = input<'button' | 'submit' | 'reset'>('button');
    disabled = input<boolean>(false);
    loading = input<boolean>(false);
    fullWidth = input<boolean>(false);
    clicked = output<void>();

    onClick() {
        if (!this.disabled() && !this.loading()) {
            this.clicked.emit();
        }
    }

    get classes(): string {
        const base = 'inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring cursor-pointer select-none';
        const sizes: Record<ButtonSize, string> = {
            sm: 'h-8 px-3 text-sm',
            md: 'h-10 px-5 text-sm',
            lg: 'h-12 px-7 text-base',
        };
        const variants: Record<ButtonVariant, string> = {
            primary: 'bg-primary text-foreground-inverse hover:bg-primary-hover shadow-sm hover:shadow-md active:scale-[0.98]',
            secondary: 'bg-surface-elevated border border-border text-foreground hover:bg-border hover:border-border-strong',
            ghost: 'text-foreground-muted hover:text-foreground hover:bg-surface-elevated',
            danger: 'bg-danger text-white hover:opacity-90',
        };
        const width = this.fullWidth() ? 'w-full' : '';
        const disabledCls = this.disabled() || this.loading() ? 'opacity-50 cursor-not-allowed' : '';
        return `${base} ${sizes[this.size()]} ${variants[this.variant()]} ${width} ${disabledCls}`.trim();
    }
}
