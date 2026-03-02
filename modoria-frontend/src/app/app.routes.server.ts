import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    // Auth pages rendered on client side only (no SSR for login/register)
    path: 'auth/**',
    renderMode: RenderMode.Client,
  },
  {
    // All other routes use SSR
    path: '**',
    renderMode: RenderMode.Server,
  },
];
