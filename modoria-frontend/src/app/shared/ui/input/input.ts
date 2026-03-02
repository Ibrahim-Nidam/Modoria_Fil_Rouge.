import { Component, input, signal, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';

export type InputType = 'text' | 'email' | 'password' | 'number' | 'tel';

@Component({
    selector: 'ui-input',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './input.html',
    styleUrl: './input.css',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => Input),
            multi: true,
        },
    ],
})
export class Input implements ControlValueAccessor {
    label = input<string>('');
    placeholder = input<string>('');
    type = input<InputType>('text');
    errorMessage = input<string>('');
    hint = input<string>('');
    icon = input<string>('');

    protected value = signal<string>('');
    protected showPassword = signal(false);
    protected isFocused = signal(false);
    protected onChange: (v: string) => void = () => { };
    protected onTouched: () => void = () => { };

    get activeType(): InputType {
        if (this.type() === 'password') return this.showPassword() ? 'text' : 'password';
        return this.type();
    }

    onInput(event: Event) {
        const val = (event.target as HTMLInputElement).value;
        this.value.set(val);
        this.onChange(val);
    }

    onFocus() { this.isFocused.set(true); }
    onBlur() { this.isFocused.set(false); this.onTouched(); }
    togglePassword() { this.showPassword.update(v => !v); }

    get wrapperClasses(): string {
        if (this.errorMessage()) {
            return 'border-danger focus-within:ring-2 focus-within:ring-danger/30';
        }
        return 'border-border focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/20';
    }

    // Keep inputClasses for potential backward compatibility
    get inputClasses(): string { return ''; }

    writeValue(val: string): void { this.value.set(val ?? ''); }
    registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
    registerOnTouched(fn: () => void): void { this.onTouched = fn; }
}
