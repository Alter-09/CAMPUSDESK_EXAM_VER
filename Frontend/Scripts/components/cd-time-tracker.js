import { logTicketTime, getTicketTimeLogs } from "../services/time-tracking.service.js";

// [Nueva Funcionalidad: T-13/T-08 - Cronómetro y Registro de Tiempo] - Permite contabilizar y registrar horas invertidas en un tiquete - Conecta con: time-tracking.service.js y <cd-time-tracker>
class CdTimeTracker extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.ticketId = null;
    this.timerInterval = null;
    this.elapsedSeconds = 0;
    this.isRunning = false;
    this.logs = [];
  }

  static get observedAttributes() {
    return ["ticket-id"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "ticket-id" && newValue) {
      this.ticketId = newValue;
      this.loadLogs();
    }
  }

  connectedCallback() {
    this.render();
    if (this.ticketId) {
      this.loadLogs();
    }
  }

  disconnectedCallback() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  async loadLogs() {
    if (!this.ticketId) return;
    try {
      this.logs = await getTicketTimeLogs(this.ticketId);
      this.render();
    } catch (err) {
      console.warn("cd-time-tracker: Could not load logs", err);
    }
  }

  formatSeconds(secs) {
    const h = String(Math.floor(secs / 3600)).padStart(2, "0");
    const m = String(Math.floor((secs % 3600) / 60)).padStart(2, "0");
    const s = String(secs % 60).padStart(2, "0");
    return `${h}:${m}:${s}`;
  }

  startTimer() {
    if (this.isRunning) return;
    this.isRunning = true;
    this.timerInterval = setInterval(() => {
      this.elapsedSeconds++;
      const timerDisplay = this.shadowRoot.getElementById("timer-display");
      if (timerDisplay) {
        timerDisplay.textContent = this.formatSeconds(this.elapsedSeconds);
      }
    }, 1000);
    this.renderTimerControls();
  }

  pauseTimer() {
    if (!this.isRunning) return;
    this.isRunning = false;
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.renderTimerControls();
  }

  resetTimer() {
    this.pauseTimer();
    this.elapsedSeconds = 0;
    const timerDisplay = this.shadowRoot.getElementById("timer-display");
    if (timerDisplay) {
      timerDisplay.textContent = "00:00:00";
    }
    this.renderTimerControls();
  }

  async submitLog(minutes, description, isBillable) {
    if (!this.ticketId) {
      alert("No hay un tiquete asignado para registrar tiempo.");
      return;
    }
    if (!minutes || minutes <= 0) {
      alert("El tiempo a registrar debe ser superior a 0 minutos.");
      return;
    }
    try {
      await logTicketTime(this.ticketId, {
        timeSpentMinutes: parseInt(minutes, 10),
        description: description || "Trabajo técnico en tiquete",
        billable: Boolean(isBillable)
      });
      this.resetTimer();
      await this.loadLogs();
      this.dispatchEvent(new CustomEvent("time-logged", { bubbles: true, composed: true, detail: { ticketId: this.ticketId, minutes } }));
    } catch (err) {
      alert("Error registrando tiempo: " + (err.message || err));
    }
  }

  renderTimerControls() {
    const btnPlay = this.shadowRoot.getElementById("btn-toggle-play");
    if (btnPlay) {
      btnPlay.textContent = this.isRunning ? "Pausar" : "Iniciar";
      btnPlay.className = this.isRunning ? "btn btn--warning" : "btn btn--primary";
    }
  }

  render() {
    const totalMinutes = this.logs.reduce((acc, curr) => acc + (curr.timeSpentMinutes || 0), 0);
    const totalHours = (totalMinutes / 60).toFixed(1);

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          font-family: inherit;
          color: #0f172a;
        }
        .tracker-card {
          background: #ffffff;
          border-radius: 12px;
          border: 1px solid #e2e8f0;
          padding: 16px;
          box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }
        .header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;
        }
        .title {
          font-size: 0.95rem;
          font-weight: 700;
          color: #1e293b;
          margin: 0;
          display: flex;
          align-items: center;
          gap: 6px;
        }
        .total-badge {
          background: #eff6ff;
          color: #2563eb;
          font-weight: 600;
          font-size: 0.8rem;
          padding: 3px 8px;
          border-radius: 999px;
        }
        .timer-box {
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: #f8fafc;
          border-radius: 8px;
          padding: 12px;
          margin-bottom: 12px;
        }
        .clock {
          font-family: monospace;
          font-size: 1.5rem;
          font-weight: 700;
          color: #0f172a;
        }
        .btn-group {
          display: flex;
          gap: 6px;
        }
        .btn {
          border: none;
          border-radius: 6px;
          padding: 6px 12px;
          font-size: 0.825rem;
          font-weight: 600;
          cursor: pointer;
          transition: background 0.15s;
        }
        .btn--primary { background: #2563eb; color: #ffffff; }
        .btn--primary:hover { background: #1d4ed8; }
        .btn--warning { background: #f59e0b; color: #ffffff; }
        .btn--warning:hover { background: #d97706; }
        .btn--secondary { background: #e2e8f0; color: #475569; }
        .btn--secondary:hover { background: #cbd5e1; }
        .manual-form {
          display: flex;
          flex-direction: column;
          gap: 8px;
          border-top: 1px dashed #e2e8f0;
          padding-top: 12px;
        }
        .form-row {
          display: flex;
          gap: 8px;
        }
        input, textarea {
          border: 1px solid #cbd5e1;
          border-radius: 6px;
          padding: 6px 10px;
          font-size: 0.85rem;
          font-family: inherit;
        }
        input[type="number"] { width: 90px; }
        input[type="text"], textarea { flex: 1; }
        .logs-list {
          margin-top: 12px;
          max-height: 150px;
          overflow-y: auto;
          font-size: 0.8rem;
          display: flex;
          flex-direction: column;
          gap: 6px;
        }
        .log-item {
          display: flex;
          justify-content: space-between;
          padding: 4px 6px;
          background: #f1f5f9;
          border-radius: 4px;
        }
      </style>

      <div class="tracker-card">
        <div class="header">
          <h4 class="title">⏱️ Control de Tiempo</h4>
          <span class="total-badge">${totalMinutes} min (${totalHours}h)</span>
        </div>

        <div class="timer-box">
          <span class="clock" id="timer-display">${this.formatSeconds(this.elapsedSeconds)}</span>
          <div class="btn-group">
            <button class="btn ${this.isRunning ? "btn--warning" : "btn--primary"}" id="btn-toggle-play">
              ${this.isRunning ? "Pausar" : "Iniciar"}
            </button>
            <button class="btn btn--secondary" id="btn-reset">Reiniciar</button>
            <button class="btn btn--primary" id="btn-save-timer">Registrar</button>
          </div>
        </div>

        <div class="manual-form">
          <div class="form-row">
            <input type="number" id="manual-mins" placeholder="Minutos" min="1" />
            <input type="text" id="manual-desc" placeholder="Detalle de actividad..." />
            <button class="btn btn--secondary" id="btn-save-manual">+ Agregar</button>
          </div>
        </div>

        ${this.logs.length > 0 ? `
          <div class="logs-list">
            ${this.logs.map(log => `
              <div class="log-item">
                <span><b>${log.timeSpentMinutes} min</b> - ${log.description || "Sin descripción"}</span>
                <span style="color:#64748b;">${log.technicianUsername || ""}</span>
              </div>
            `).join("")}
          </div>
        ` : ""}
      </div>
    `;

    this.attachEvents();
  }

  attachEvents() {
    this.shadowRoot.getElementById("btn-toggle-play")?.addEventListener("click", () => {
      if (this.isRunning) this.pauseTimer();
      else this.startTimer();
    });

    this.shadowRoot.getElementById("btn-reset")?.addEventListener("click", () => {
      this.resetTimer();
    });

    this.shadowRoot.getElementById("btn-save-timer")?.addEventListener("click", () => {
      const minutes = Math.ceil(this.elapsedSeconds / 60);
      if (minutes <= 0) {
        alert("El cronómetro debe sumar al menos 1 minuto para registrarse.");
        return;
      }
      this.submitLog(minutes, "Sesión de cronómetro", true);
    });

    this.shadowRoot.getElementById("btn-save-manual")?.addEventListener("click", () => {
      const minsInput = this.shadowRoot.getElementById("manual-mins");
      const descInput = this.shadowRoot.getElementById("manual-desc");
      const mins = parseInt(minsInput.value, 10);
      const desc = descInput.value.trim();
      if (!mins || mins <= 0) return;
      this.submitLog(mins, desc, true);
      minsInput.value = "";
      descInput.value = "";
    });
  }
}

if (!customElements.get("cd-time-tracker")) {
  customElements.define("cd-time-tracker", CdTimeTracker);
}

export default CdTimeTracker;
