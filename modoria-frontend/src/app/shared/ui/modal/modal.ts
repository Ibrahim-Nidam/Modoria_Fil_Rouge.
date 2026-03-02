import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'ui-modal',
    standalone: true,
    templateUrl: './modal.html',
    styleUrl: './modal.css'
})
export class Modal {
    @Input() isOpen = false;
    @Input() title = '';
    @Output() close = new EventEmitter<void>();

    onClose() {
        this.close.emit();
    }
}
