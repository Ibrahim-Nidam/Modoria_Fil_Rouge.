import { Component, input } from '@angular/core';

@Component({
    selector: 'ui-spinner',
    standalone: true,
    templateUrl: './spinner.html',
    styleUrl: './spinner.css',
})
export class Spinner {
    size = input<'sm' | 'md' | 'lg'>('md');
    color = input<'primary' | 'white'>('primary');

    get sizeClass(): string {
        return { sm: 'w-4 h-4', md: 'w-6 h-6', lg: 'w-10 h-10' }[this.size()];
    }

    get colorClass(): string {
        return this.color() === 'white' ? 'text-white' : 'text-primary';
    }
}
