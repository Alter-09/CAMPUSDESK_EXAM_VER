import { apiRequest } from "../config/http-client.js";

class TicketList extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
    this.tickets = [];
    this.loading = true;
    this.error = null;
  }

  async connectedCallback() {
    this.renderLoading();
    await this.fetchTickets();
  }

  async fetchTickets() {
    try {
      const data = await apiRequest("/api/tickets", { method: "GET" });
      
      // Mapeo con los campos exactos del TicketDto del backend Spring Boot:
      // applicantUsername, technicianUsername, title, status (enum: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED)
      this.tickets = Array.isArray(data) ? data.map(ticket => ({
        id: ticket.id ?? ticket.ticketId ?? '---',
        requester: ticket.applicantUsername || ticket.requester || ticket.solicitante || 'Desconocido',
        subject: ticket.title || ticket.subject || ticket.asunto || 'Sin asunto',
        technician: ticket.technicianUsername || ticket.technician
          || (ticket.tecnico ? ticket.tecnico.nombre : null)
          || 'Sin asignar',
        status: ticket.status || ticket.estado || 'Pendiente',
        statusCode: this.mapStatusCode(ticket.status || ticket.estado)
      })) : [];

      this.loading = false;
      this.render();
    } catch (err) {
      console.error("Error al obtener tiquetes:", err);
      this.loading = false;
      this.error = err.message || 'No se pudieron sincronizar los datos con el servidor.';
      this.render();
    }
  }

  mapStatusCode(statusText) {
    if (!statusText) return 'open';
    // Mapeo directo con los valores del enum del backend (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED)
    const STATUS_MAP = {
      'OPEN':        'open',
      'ASSIGNED':    'assigned',
      'IN_PROGRESS': 'in-progress',
      'RESOLVED':    'resolved',
      'CLOSED':      'closed',
    };
    const upper = statusText.toUpperCase().trim();
    return STATUS_MAP[upper] ?? 'open';
  }

  renderLoading() {
    this.shadowRoot.innerHTML = `
      <style>${this.getStyles()}</style>
      <section class="ticket-directory">
        <div class="ticket-directory__header">
          <p class="ticket-directory__eyebrow">Monitoreo Global</p>
          <h1 class="ticket-directory__title">Estado General de Tiquetes</h1>
        </div>
        <p class="ticket-directory__message">Cargando tiquetes del sistema...</p>
      </section>
    `;
  }

  render() {
    if (this.error) {
      this.shadowRoot.innerHTML = `
        <style>${this.getStyles()}</style>
        <section class="ticket-directory">
          <div class="ticket-directory__header">
            <p class="ticket-directory__eyebrow">Monitoreo Global</p>
            <h1 class="ticket-directory__title">Estado General de Tiquetes</h1>
          </div>
          <p class="ticket-directory__error">${this.error}</p>
        </section>
      `;
      return;
    }

    this.shadowRoot.innerHTML = `
      <style>${this.getStyles()}</style>
      <section class="ticket-directory">
        <div class="ticket-directory__header">
          <p class="ticket-directory__eyebrow">Monitoreo Global</p>
          <h1 class="ticket-directory__title">Estado General de Tiquetes</h1>
        </div>
        
        <div class="ticket-table-wrapper" role="region" aria-label="Estado general de tiquetes" tabindex="0">
          <table class="ticket-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Solicitante</th>
                <th>Asunto / Categoría</th>
                <th>Técnico Asignado</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              ${this.tickets.length > 0 ? this.tickets.map(ticket => `
                <tr>
                  <td>${ticket.id}</td>
                  <td>${ticket.requester}</td>
                  <td>${ticket.subject}</td>
                  <td>${ticket.technician}</td>
                  <td>
                    <span class="status-badge status-badge--${ticket.statusCode}">
                      ${ticket.status}
                    </span>
                  </td>
                </tr>
              `).join('') : `
                <tr>
                  <td colspan="5" style="text-align: center; padding: 2.5rem; color: #cbd5e1;">
                    No hay tiquetes registrados en el sistema.
                  </td>
                </tr>
              `}
            </tbody>
          </table>
        </div>
      </section>
    `;
  }

  getStyles() {
    return `
      :host {
        display: block;
        width: 100%;
      }

      .ticket-directory {
        background-color: var(--cd-color-surface-card, #062654);
        border: 1px solid var(--cd-color-border-subtle, rgba(203, 213, 225, 0.12));
        border-radius: 0.75rem;
        padding: 2rem;
        box-shadow: 0 12px 32px -4px rgba(1, 14, 36, 0.7), 0 0 0 1px rgba(203, 213, 225, 0.15);
        max-width: 950px;
        margin: 0 auto;
        box-sizing: border-box;
      }

      .ticket-directory__header {
        margin-bottom: 1.5rem;
        text-align: center;
      }

      .ticket-directory__eyebrow {
        font-family: var(--cd-font-display, sans-serif);
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--cd-color-signal-coral, #e75c69);
        font-weight: 600;
        margin: 0 0 0.25rem 0;
      }

      .ticket-directory__title {
        font-family: var(--cd-font-display, sans-serif);
        font-size: 1.5rem;
        font-weight: 700;
        color: #f1f5f9;
        margin: 0;
      }

      .ticket-directory__message,
      .ticket-directory__error {
        text-align: center;
        color: #cbd5e1;
        font-size: 0.95rem;
        padding: 1rem 0;
        margin: 0;
      }

      .ticket-directory__error {
        color: #ff6b79;
      }

      .ticket-table-wrapper {
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 8px;
        overflow-x: auto;
        width: 100%;
      }

      .ticket-table {
        width: 100%;
        border-collapse: collapse;
        text-align: left;
        font-size: 0.9rem;
      }

      .ticket-table th,
      .ticket-table td {
        padding: 0.85rem 1rem;
        vertical-align: middle;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      }

      .ticket-table th {
        background: rgba(255, 255, 255, 0.05);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        color: #94a3b8;
        font-weight: 600;
        font-size: 0.8125rem;
        text-transform: uppercase;
        letter-spacing: 0.03em;
      }

      .ticket-table td {
        color: #cbd5e1;
      }

      .ticket-table td:first-child {
        color: #f8fafc;
        font-weight: 500;
      }

      .status-badge {
        display: inline-block;
        padding: 0.25rem 0.5rem;
        font-size: 0.75rem;
        font-weight: 600;
        border-radius: 4px;
        text-transform: uppercase;
        letter-spacing: 0.02em;
      }

      /* OPEN */
      .status-badge--open {
        background: rgba(99, 102, 241, 0.2);
        color: #a5b4fc;
        border: 1px solid rgba(99, 102, 241, 0.35);
      }

      /* ASSIGNED */
      .status-badge--assigned {
        background: rgba(234, 179, 8, 0.2);
        color: #fde047;
        border: 1px solid rgba(234, 179, 8, 0.35);
      }

      /* IN_PROGRESS */
      .status-badge--in-progress {
        background: rgba(245, 158, 11, 0.2);
        color: #fcd34d;
        border: 1px solid rgba(245, 158, 11, 0.35);
      }

      /* RESOLVED */
      .status-badge--resolved {
        background: rgba(16, 185, 129, 0.2);
        color: #6ee7b7;
        border: 1px solid rgba(16, 185, 129, 0.35);
      }

      /* CLOSED */
      .status-badge--closed {
        background: rgba(100, 116, 139, 0.2);
        color: #94a3b8;
        border: 1px solid rgba(100, 116, 139, 0.35);
      }

      @media screen and (max-width: 768px) {
        .ticket-directory {
          padding: 1rem;
        }

        .ticket-table-wrapper {
          scrollbar-width: none;
          -ms-overflow-style: none;
        }

        .ticket-table-wrapper::-webkit-scrollbar {
          display: none;
        }

        .ticket-table {
          display: block;
          min-width: 550px;
          white-space: nowrap;
        }
      }
    `;
  }
}

customElements.define('cd-ticket-list', TicketList);