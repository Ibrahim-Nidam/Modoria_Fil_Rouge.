import { Component, Input, Output, EventEmitter } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

@Component({
    selector: 'ui-toast',
    standalone: true,
    templateUrl: './toast.html',
    styleUrl: './toast.css'
})
export class Toast {
    @Input() type: ToastType = 'info';
    @Input() title = '';
    @Input() message = '';
    @Output() close = new EventEmitter<void>();

    onClose() {
        this.close.emit();
    }
}
