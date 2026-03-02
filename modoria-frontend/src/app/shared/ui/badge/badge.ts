import { Component, Input } from '@angular/core';

@Component({
    selector: 'ui-badge',
    standalone: true,
    templateUrl: './badge.html',
    styleUrl: './badge.css'
})
export class Badge {
    @Input() variant: 'default' | 'success' | 'warning' | 'danger' | 'info' = 'default';
    @Input() size: 'sm' | 'md' | 'lg' = 'md';
}
