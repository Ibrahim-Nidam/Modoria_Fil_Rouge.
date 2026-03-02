import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
    selector: 'app-admin-sidebar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive],
    templateUrl: './admin-sidebar.html',
    styleUrl: './admin-sidebar.css'
})
export class AdminSidebar {
    navItems = [
        { label: 'Dashboard', path: '/admin/dashboard', icon: 'layout-dashboard' },
        { label: 'Products', path: '/admin/products', icon: 'package' },
        { label: 'Categories', path: '/admin/categories', icon: 'folder-tree' },
        { label: 'Seasons', path: '/admin/seasons', icon: 'sun-snow' },
        { label: 'Orders', path: '/admin/orders', icon: 'shopping-bag' },
        { label: 'Coupons', path: '/admin/coupons', icon: 'ticket' },
        { label: 'Users', path: '/admin/users', icon: 'users' },
        { label: 'Support', path: '/admin/support', icon: 'headset' },
    ];
}
