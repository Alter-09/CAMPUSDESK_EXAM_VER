import { submitCsat } from "../services/csat.service.js";

// [Nueva Funcionalidad: U-06 - Modal de Calificación CSAT] - Captura puntuación 1-5 estrellas y feedback - Conecta con: csat.service.js y <cd-csat-modal>
class CdCsatModal extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.ticketId = null;
    this.selectedScore = "VERY_SATISFIED";
  }

  static get observedAttributes() {
    return ["ticket-id", "open"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "ticket-id") this.ticketId = newValue;
    if (name === "open") this.render();
  }

  connectedCallback() {
    this.render();
  }

  render() {
    const isOpen = this.getAttribute("open") === "true";
    if (!isOpen) {
      this.shadowRoot.innerHTML = "";
      return;
    }

    this.shadowRoot.innerHTML = `
      <style>
        .overlay {
          position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6);
          display: flex; align-items: center; justify-content: center; z-index: 9999;
          backdrop-filter: blur(4px);
        }
        .modal {
          background: #ffffff; border-radius: 16px; padding: 28px; width: 440px;
          max-width: 90vw; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
          font-family: inherit; color: #1e293b;
        }
        h3 { margin: 0 0 8px 0; font-size: 1.25rem; font-weight: 700; color: #0f172a; }
        p { margin: 0 0 20px 0; font-size: 0.9rem; color: #64748b; }
        .stars { display: flex; gap: 8px; justify-content: center; margin-bottom: 20px; }
        .star-btn {
          background: none; border: none; font-size: 2rem; cursor: pointer;
          color: #cbd5e1; transition: transform 0.15s, color 0.15s;
        }
        .star-btn:hover, .star-btn.active { color: #f59e0b; transform: scale(1.15); }
        textarea {
          width: 100%; box-sizing: border-box; border: 1px solid #cbd5e1; border-radius: 10px;
          padding: 10px 14px; font-family: inherit; font-size: 0.9rem; resize: vertical; min-height: 80px;
        }
        .actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
        button {
          padding: 10px 18px; border-radius: 8px; font-weight: 600; cursor: pointer; border: none; font-size: 0.9rem;
        }
        .btn-cancel { background: #f1f5f9; color: #475569; }
        .btn-submit { background: #3b82f6; color: white; }
        .btn-submit:hover { background: #2563eb; }
      </style>
      <div class="overlay">
        <div class="modal">
          <h3>¿Cómo calificarías la atención recibida?</h3>
          <p>Tu opinión nos ayuda a mejorar el servicio de soporte técnico universitario.</p>
          <div class="stars">
            <button class="star-btn" data-score="VERY_DISSATISFIED" title="Muy Insatisfecho">★</button>
            <button class="star-btn" data-score="DISSATISFIED" title="Insatisfecho">★</button>
            <button class="star-btn" data-score="NEUTRAL" title="Neutral">★</button>
            <button class="star-btn" data-score="SATISFIED" title="Satisfecho">★</button>
            <button class="star-btn active" data-score="VERY_SATISFIED" title="Muy Satisfecho">★</button>
          </div>
          <textarea id="feedback" placeholder="Comentarios adicionales sobre la solución o el técnico (opcional)..."></textarea>
          <div class="actions">
            <button class="btn-cancel" id="btn-cancel">Omitir</button>
            <button class="btn-submit" id="btn-submit">Enviar Calificación</button>
          </div>
        </div>
      </div>
    `;

    this.attachEvents();
  }

  attachEvents() {
    const starBtns = this.shadowRoot.querySelectorAll(".star-btn");
    starBtns.forEach((btn, index) => {
      btn.addEventListener("click", () => {
        this.selectedScore = btn.dataset.score;
        starBtns.forEach((b, i) => {
          if (i <= index) b.classList.add("active");
          else b.classList.remove("active");
        });
      });
    });

    this.shadowRoot.getElementById("btn-cancel")?.addEventListener("click", () => {
      this.removeAttribute("open");
    });

    this.shadowRoot.getElementById("btn-submit")?.addEventListener("click", async () => {
      const feedback = this.shadowRoot.getElementById("feedback")?.value ?? "";
      try {
        await submitCsat(this.ticketId, { score: this.selectedScore, feedback });
        this.dispatchEvent(new CustomEvent("csat-submitted", { detail: { ticketId: this.ticketId } }));
        this.removeAttribute("open");
      } catch (err) {
        alert("Error al enviar calificación: " + err.message);
      }
    });
  }
}

customElements.define("cd-csat-modal", CdCsatModal);
