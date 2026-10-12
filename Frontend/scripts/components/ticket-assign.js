import { apiRequest } from "../config/http-client.js";

class TicketAssign extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: 'open' });
    this.tickets = [];
    this.technicians = [];
    this.selectedTicket = null;
    this.submitting = false;
    this.error = null;
    this.successMessage = null;
  }

  async connectedCallback() {
    this.renderLoading();
    await this.loadData();
  }

  async loadData() {
    try {
      const ticketsData = await apiRequest("/api/tickets?status=OPEN", { method: "GET" });
      this.tickets = Array.isArray(ticketsData) ? ticketsData : [];

      const usersData = await apiRequest("/api/user/all", { method: "GET" });
      const usersList = Array.isArray(usersData) ? usersData : (usersData?.content || usersData?.users || []);

      this.technicians = usersList.filter(user => {
        const role = user.role || user.rol || (user.roles && String(user.roles)) || '';
        const upperRole = String(role).toUpperCase();
        return upperRole.includes('TECH') || upperRole.includes('TECNICO') || upperRole.includes('SUPPORT') || true;
      });

      this.render();
      this.attachEvents();
    } catch (err) {
      console.error("Error al cargar datos:", err);
      this.error = err.message || 'No se pudieron sincronizar los datos del servidor.';
      this.render();
    }
  }

  attachEvents() {
    const ticketSelect = this.shadowRoot.getElementById('select-ticket');
    const form = this.shadowRoot.getElementById('assign-tech-form');

    if (ticketSelect) {
      ticketSelect.addEventListener('change', (e) => {
        const target = /** @type {HTMLSelectElement} */ (e.target);
        const ticketId = target.value;
        this.selectedTicket = this.tickets.find(t => String(t.id) === String(ticketId));
        this.render();
        this.attachEvents();
      });
    }

    if (form) {
      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await this.handleAssign();
      });
    }
  }

  async handleAssign() {
    const ticketId = /** @type {HTMLSelectElement} */ (this.shadowRoot.getElementById('select-ticket'))?.value;
    const technicianUsername = /** @type {HTMLSelectElement} */ (this.shadowRoot.getElementById('select-technician'))?.value;

    if (!ticketId || !technicianUsername) {
      alert("Por favor selecciona un tiquete y un técnico disponible.");
      return;
    }

    this.submitting = true;
    this.error = null;
    this.successMessage = null;
    this.render();
    this.attachEvents();

    try {
      await apiRequest(`/api/tickets/${ticketId}/transitions`, {
        method: "POST",
        body: {
          action: "ASSIGN",
          technicianUsername: technicianUsername
        }
      });

      this.successMessage = "¡Tiquete asignado exitosamente al técnico!";
      this.submitting = false;
      this.selectedTicket = null;
      await this.loadData();
    } catch (err) {
      console.error("Error al asignar:", err);
      this.submitting = false;
      this.error = err.message || "Error al procesar la asignación.";
      this.render();
      this.attachEvents();
    }
  }

  renderLoading() {
    this.shadowRoot.innerHTML = `
      <link rel="stylesheet" href="./stylesheet/components/ticket-assign.css">
      <div class="assign-wrapper">
        <div class="ticket-directory__header">
          <p class="ticket-directory__eyebrow">Gestión Operativa</p>
          <h1 class="ticket-directory__title">Asignación de Técnico a Tiquete</h1>
        </div>
        <p style="text-align: center; color: #cbd5e1; margin-top: 2rem;">Cargando información...</p>
      </div>
    `;
  }

  render() {
    const ticketsOptions = this.tickets.map(t => 
      `<option value="${t.id}" ${this.selectedTicket && String(this.selectedTicket.id) === String(t.id) ? 'selected' : ''}>` +
      `#${t.id} - ${t.title || t.subject || 'Sin título'} (${t.applicantUsername || t.solicitante || 'Usuario'})</option>`
    ).join('');

    const techOptions = this.technicians.map(tech => 
      `<option value="${tech.username || tech.email}">` +
      `${tech.fullName || tech.name || tech.username || 'Técnico'} (${tech.email || tech.username})</option>`
    ).join('');

    this.shadowRoot.innerHTML = `
      <link rel="stylesheet" href="./stylesheet/components/ticket-assign.css">
      <div class="assign-wrapper">
        <div class="ticket-directory__header">
          <p class="ticket-directory__eyebrow">Gestión Operativa</p>
          <h1 class="ticket-directory__title">Asignación de Técnico a Tiquete</h1>
        </div>
        
        ${this.error ? `<div class="alert alert--error">${this.error}</div>` : ''}
        ${this.successMessage ? `<div class="alert alert--success">${this.successMessage}</div>` : ''}

        <form id="assign-tech-form" novalidate>
          <div class="fields-container">
              
              <div class="field">
                  <label class="field__label" for="select-ticket">Seleccionar Tiquete Pendiente</label>
                  <div class="field__control">
                      <select id="select-ticket" class="field__input" required>
                          <option value="" disabled ${!this.selectedTicket ? 'selected' : ''}>Elige un tiquete sin asignar...</option>
                          ${ticketsOptions}
                      </select>
                  </div>
              </div>

              <div class="ticket-details-card" style="${this.selectedTicket ? 'display: block;' : 'display: none;'}">
                  <p class="ticket-details__title">Detalles del caso seleccionado:</p>
                  <p class="ticket-details__text"><strong>Prioridad:</strong> ${this.selectedTicket?.priority || '---'}</p>
                  <p class="ticket-details__text"><strong>Categoría:</strong> ${this.selectedTicket?.category || '---'}</p>
                  <p class="ticket-details__text"><strong>Descripción:</strong> ${this.selectedTicket?.description || '---'}</p>
              </div>

              <div class="field">
                  <label class="field__label" for="select-technician">Asignar Técnico de Soporte</label>
                  <div class="field__control">
                      <select id="select-technician" class="field__input" required>
                          <option value="" disabled selected>Selecciona un técnico disponible...</option>
                          ${techOptions}
                      </select>
                  </div>
              </div>

          </div>

          <div style="margin-top: 1.5rem;">
              <button class="button button--primary" type="submit" ${this.submitting ? 'aria-busy="true"' : ''}>
                  <span class="button__label">${this.submitting ? 'Asignando...' : 'Confirmar y Asignar Tiquete'}</span>
              </button>
          </div>
        </form>
      </div>
    `;
  }
}

customElements.define('cd-ticket-assign', TicketAssign);