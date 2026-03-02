import { Component } from '@angular/core';

@Component({
    selector: 'app-home',
    standalone: true,
    template: `
        <div class="flex flex-col items-center justify-center min-h-screen bg-surface">
            <h1 class="text-4xl font-bold text-primary mb-4">Welcome to Modoria</h1>
            <p class="text-foreground-subtle text-lg">Your intelligent & immersive e-commerce platform.</p>
        </div>
    `,
})
export class Home { }
