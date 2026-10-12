/**
 * DEPRECATED — user-nav.js
 *
 * Este módulo gestionaba el enrutamiento por hash (#create-ticket, #my-tickets)
 * dentro de user.html cuando el portal era un SPA de página única.
 *
 * Con la refactorización MPA (Multi-Page Application), cada vista del portal
 * de usuario tiene su propio archivo HTML:
 *   - user.html          → Inicio / Overview
 *   - create-ticket.html → Crear nuevo Tiquete
 *   - my-tickets.html    → Mis Solicitudes
 *
 * La navegación ahora funciona a través de <a href="..."> normales.
 * La inicialización de cabecera se maneja en scripts/utils/user-page.utils.js
 * y es importada por cada página en scripts/pages/.
 *
 * Este archivo ya no es referenciado por ningún HTML.
 * Se conserva como referencia histórica y puede eliminarse en el próximo ciclo.
 */
