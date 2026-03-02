import { Component, Input } from '@angular/core';

@Component({
    selector: 'ui-card',
    standalone: true,
    templateUrl: './card.html',
    styleUrl: './card.css'
})
export class Card {
    @Input() padding: 'none' | 'sm' | 'md' | 'lg' = 'md';
    @Input() elevated = false;
}
