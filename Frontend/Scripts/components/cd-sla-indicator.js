// [Nueva Funcionalidad: T-01/A-03 - Indicador Visual de SLA] - Visualiza cuenta regresiva y semáforo de cumplimiento SLA - Conecta con: SlaPolicy y <cd-sla-indicator>
class CdSlaIndicator extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.timerInterval = null;
  }

  static get observedAttributes() {
    return ["due-date", "breached", "priority", "status"];
  }

  attributeChangedCallback() {
    this.render();
  }

  connectedCallback() {
    this.render();
    this.timerInterval = setInterval(() => this.render(), 60000);
  }

  disconnectedCallback() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  render() {
    const dueDateStr = this.getAttribute("due-date");
    const isBreached = this.getAttribute("breached") === "true";
    const status = (this.getAttribute("status") || "").toUpperCase();

    if (!dueDateStr) {
      this.shadowRoot.innerHTML = `
        <style>
          .badge-none { font-size: 0.75rem; color: #94a3b8; font-style: italic; }
        </style>
        <span class="badge-none">Sin SLA</span>
      `;
      return;
    }

    const dueDate = new Date(dueDateStr);
    const now = new Date();
    const diffMs = dueDate.getTime() - now.getTime();
    const isPast = diffMs < 0;

    const isResolvedOrClosed = ["RESOLVED", "CLOSED", "CANCELLED"].includes(status);

    let theme = "theme-normal";
    let icon = "⏱️";
    let label = "";

    if (isResolvedOrClosed) {
      theme = isBreached ? "theme-breached" : "theme-completed";
      icon = isBreached ? "⚠️" : "✅";
      label = isBreached ? "SLA Incumplido" : "SLA Cumplido";
    } else if (isBreached || isPast) {
      theme = "theme-breached";
      icon = "🚨";
      const pastMins = Math.abs(Math.floor(diffMs / 60000));
      const hours = Math.floor(pastMins / 60);
      const mins = pastMins % 60;
      label = `SLA Vencido (${hours > 0 ? `${hours}h ` : ""}${mins}m)`;
    } else {
      const remainingMins = Math.floor(diffMs / 60000);
      const hours = Math.floor(remainingMins / 60);
      const mins = remainingMins % 60;

      if (remainingMins <= 120) {
        theme = "theme-risk";
        icon = "⚠️";
        label = `En Riesgo (${hours > 0 ? `${hours}h ` : ""}${mins}m)`;
      } else {
        theme = "theme-normal";
        icon = "⏱️";
        label = `SLA (${hours > 0 ? `${hours}h ` : ""}${mins}m)`;
      }
    }

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: inline-block;
          font-family: inherit;
        }
        .sla-pill {
          display: inline-flex;
          align-items: center;
          gap: 5px;
          padding: 3px 9px;
          border-radius: 999px;
          font-size: 0.75rem;
          font-weight: 600;
          cursor: help;
          transition: transform 0.15s;
        }
        .sla-pill:hover {
          transform: translateY(-1px);
        }
        .theme-normal {
          background: #ecfdf5;
          color: #065f46;
          border: 1px solid #a7f3d0;
        }
        .theme-risk {
          background: #fffbeb;
          color: #92400e;
          border: 1px solid #fde68a;
          animation: pulse 2s infinite;
        }
        .theme-breached {
          background: #fef2f2;
          color: #991b1b;
          border: 1px solid #fecaca;
        }
        .theme-completed {
          background: #f1f5f9;
          color: #475569;
          border: 1px solid #e2e8f0;
        }
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.75; }
        }
      </style>

      <span class="sla-pill ${theme}" title="Vencimiento: ${dueDate.toLocaleString()}">
        <span>${icon}</span>
        <span>${label}</span>
      </span>
    `;
  }
}

if (!customElements.get("cd-sla-indicator")) {
  customElements.define("cd-sla-indicator", CdSlaIndicator);
}

export default CdSlaIndicator;
