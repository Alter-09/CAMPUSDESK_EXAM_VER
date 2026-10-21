// [Nueva Funcionalidad: U-30/Sistema de Toasts Global] - Gestor de alertas flotantes accesibles en pantalla - Conecta con: CustomEvent('app-toast') y <cd-toast-manager>
class CdToastManager extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.toasts = [];
  }

  connectedCallback() {
    this.render();
    window.addEventListener("app-toast", this.handleToastEvent.bind(this));
  }

  disconnectedCallback() {
    window.removeEventListener("app-toast", this.handleToastEvent.bind(this));
  }

  handleToastEvent(event) {
    if (!event.detail) return;
    const { type = "info", title = "", message = "" } = event.detail;
    const id = Date.now() + Math.random().toString(36).substring(2, 5);
    const toast = { id, type, title, message };
    this.toasts.push(toast);
    this.renderToasts();

    setTimeout(() => {
      this.removeToast(id);
    }, 4000);
  }

  removeToast(id) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.renderToasts();
  }

  render() {
    this.shadowRoot.innerHTML = `
      <style>
        :host {
          position: fixed;
          top: 20px;
          right: 20px;
          z-index: 9999;
          display: flex;
          flex-direction: column;
          gap: 10px;
          pointer-events: none;
          font-family: inherit;
        }
        .toast-container {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
        .toast {
          pointer-events: auto;
          display: flex;
          align-items: flex-start;
          gap: 12px;
          width: 320px;
          max-width: 90vw;
          padding: 14px 16px;
          border-radius: 10px;
          background: #ffffff;
          box-shadow: 0 10px 25px -5px rgba(0,0,0,0.15), 0 8px 10px -6px rgba(0,0,0,0.1);
          border: 1px solid #e2e8f0;
          animation: slideIn 0.25s ease-out;
          color: #1e293b;
        }
        @keyframes slideIn {
          from { opacity: 0; transform: translateX(30px); }
          to { opacity: 1; transform: translateX(0); }
        }
        .toast--success { border-left: 4px solid #10b981; }
        .toast--error { border-left: 4px solid #ef4444; }
        .toast--warning { border-left: 4px solid #f59e0b; }
        .toast--info { border-left: 4px solid #3b82f6; }
        .toast-icon { font-size: 1.25rem; flex-shrink: 0; }
        .toast-content { flex: 1; display: flex; flex-direction: column; gap: 2px; }
        .toast-title { font-size: 0.875rem; font-weight: 700; color: #0f172a; margin: 0; }
        .toast-message { font-size: 0.8rem; color: #475569; margin: 0; line-height: 1.35; }
        .toast-close {
          background: none;
          border: none;
          color: #94a3b8;
          cursor: pointer;
          font-size: 1rem;
          padding: 0 4px;
        }
        .toast-close:hover { color: #0f172a; }
      </style>
      <div class="toast-container" id="toast-container"></div>
    `;
    this.renderToasts();
  }

  renderToasts() {
    const container = this.shadowRoot.getElementById("toast-container");
    if (!container) return;
    const icons = {
      success: "✅",
      error: "❌",
      warning: "⚠️",
      info: "ℹ️"
    };

    container.innerHTML = this.toasts.map(t => `
      <div class="toast toast--${t.type}" data-id="${t.id}" role="alert">
        <span class="toast-icon">${icons[t.type] || "ℹ️"}</span>
        <div class="toast-content">
          ${t.title ? `<h5 class="toast-title">${t.title}</h5>` : ""}
          <p class="toast-message">${t.message}</p>
        </div>
        <button class="toast-close" data-close-id="${t.id}" aria-label="Cerrar notificación">&times;</button>
      </div>
    `).join("");

    container.querySelectorAll(".toast-close").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-close-id");
        if (id) this.removeToast(id);
      });
    });
  }
}

if (!customElements.get("cd-toast-manager")) {
  customElements.define("cd-toast-manager", CdToastManager);
}

export default CdToastManager;
