import { Component, Input } from '@angular/core';

export interface ColumnDef {
    key: string;
    header: string;
}

@Component({
    selector: 'ui-table',
    standalone: true,
    templateUrl: './table.html',
    styleUrl: './table.css'
})
export class Table {
    @Input() columns: ColumnDef[] = [];
    @Input() data: any[] = [];
    @Input() loading = false;
    @Input() emptyMessage = 'No results found.';
}
