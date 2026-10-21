import { assignTicket, reassignTicket, listTickets } from "../services/ticket.service.js";
import { listTechnicians } from "../services/technician.service.js";

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
    this.error = null;
    try {
      const [ticketsResult, techResult] = await Promise.allSettled([
        listTickets(),
        listTechnicians(),
      ]);

      if (ticketsResult.status === "fulfilled" && Array.isArray(ticketsResult.value)) {
        this.tickets = ticketsResult.value.filter(t => t.status === "OPEN" || t.status === "ASSIGNED");
      } else {
        this.tickets = [];
        if (ticketsResult.status === "rejected") {
          console.error("Error al cargar tiquetes:", ticketsResult.reason);
          this.error = ticketsResult.reason?.message || "Error al cargar la lista de tiquetes.";
        }
      }

      if (this.selectedTicket) {
        this.selectedTicket = this.tickets.find(t => String(t.id) === String(this.selectedTicket.id)) || null;
      }

      if (techResult.status === "fulfilled" && Array.isArray(techResult.value)) {
        this.technicians = techResult.value;
      } else {
        this.technicians = [];
        if (techResult.status === "rejected") {
          console.error("Error al cargar técnicos:", techResult.reason);
          if (!this.error) {
            this.error = techResult.reason?.message || "Error al cargar la lista de técnicos.";
          }
        }
      }

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
      // The Spring Boot backend provides dedicated endpoints:
      // - PUT /api/tickets/{ticketId}/assign: asigna incidencias en estado OPEN
      // - PUT /api/tickets/{ticketId}/reassign: reasigna incidencias en estado ASSIGNED
      if (this.selectedTicket && this.selectedTicket.status === "ASSIGNED") {
        await reassignTicket(ticketId, technicianUsername, "Reasignación administrativa");
        this.successMessage = `¡Tiquete #${ticketId} reasignado exitosamente a "${technicianUsername}"!`;
      } else {
        try {
          await assignTicket(ticketId, technicianUsername);
          this.successMessage = `¡Tiquete #${ticketId} asignado exitosamente a "${technicianUsername}"!`;
        } catch (assignErr) {
          // If already assigned in backend, reassign transparently
          const errMsg = String(assignErr?.message || "").toLowerCase();
          if (errMsg.includes("reasign") || errMsg.includes("abierta") || errMsg.includes("assigned")) {
            await reassignTicket(ticketId, technicianUsername, "Reasignación administrativa");
            this.successMessage = `¡Tiquete #${ticketId} reasignado exitosamente a "${technicianUsername}"!`;
          } else {
            throw assignErr;
          }
        }
      }

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
    // TicketDto fields: id, title, applicantUsername, priority, category, description, status, technicianUsername
    const ticketsOptions = this.tickets.map(t => {
      const isSelected = this.selectedTicket && String(this.selectedTicket.id) === String(t.id);
      const tag = t.status === "ASSIGNED"
        ? `[Asignado a ${t.technicianUsername || 'técnico'}]`
        : `[Sin asignar]`;
      return `<option value="${t.id}" ${isSelected ? 'selected' : ''}>` +
        `#${t.id} - ${t.title || t.subject || 'Sin título'} (${t.applicantUsername || t.requester || 'Usuario'}) ${tag}</option>`;
    }).join('');

    // TechnicianItem fields: username, email, specialty, availabilityStatus
    const techOptions = this.technicians.map(tech =>
      `<option value="${tech.username}">` +
      `${tech.username} (${tech.email || tech.username})</option>`
    ).join('');

    const noTechniciansMsg = this.technicians.length === 0
      ? `<p style="color:#fcd34d; font-size:0.875rem; margin-top:0.5rem;">
           ⚠ No se encontraron técnicos registrados. Verifica que existan usuarios con rol TECHNICIAN.
         </p>`
      : '';

    const isReassign = this.selectedTicket?.status === "ASSIGNED";
    const buttonText = this.submitting
      ? 'Procesando...'
      : (isReassign ? 'Confirmar y Reasignar Tiquete' : 'Confirmar y Asignar Tiquete');

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
                  <label class="field__label" for="select-ticket">Seleccionar Tiquete</label>
                  <div class="field__control">
                      <select id="select-ticket" class="field__input" required>
                          <option value="" disabled ${!this.selectedTicket ? 'selected' : ''}>Elige un tiquete...</option>
                          ${ticketsOptions}
                      </select>
                  </div>
              </div>

              <div class="ticket-details-card" style="${this.selectedTicket ? 'display: block;' : 'display: none;'}">
                  <p class="ticket-details__title">Detalles del caso seleccionado:</p>
                  <p class="ticket-details__text"><strong>Estado:</strong> ${this.selectedTicket?.status || '---'}</p>
                  <p class="ticket-details__text"><strong>Técnico Actual:</strong> ${this.selectedTicket?.technicianUsername || 'Sin asignar'}</p>
                  <p class="ticket-details__text"><strong>Prioridad:</strong> ${this.selectedTicket?.priority || '---'}</p>
                  <p class="ticket-details__text"><strong>Categoría:</strong> ${this.selectedTicket?.category || '---'}</p>
                  <p class="ticket-details__text"><strong>Descripción:</strong> ${this.selectedTicket?.description || '---'}</p>
              </div>

              <div class="field">
                  <label class="field__label" for="select-technician">Asignar Técnico de Soporte</label>
                  <div class="field__control">
                      <select id="select-technician" class="field__input" required ${this.technicians.length === 0 ? 'disabled' : ''}>
                          <option value="" disabled selected>Selecciona un técnico disponible...</option>
                          ${techOptions}
                      </select>
                  </div>
                  ${noTechniciansMsg}
              </div>

          </div>

          <div style="margin-top: 1.5rem;">
              <button class="button button--primary" type="submit" ${this.submitting || this.technicians.length === 0 ? 'disabled' : ''}>
                  <span class="button__label">${buttonText}</span>
              </button>
          </div>
        </form>
      </div>
    `;
  }
}

customElements.define('cd-ticket-assign', TicketAssign);