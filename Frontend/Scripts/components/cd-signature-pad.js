// [Nueva Funcionalidad: T-20 - Captura de Firma Táctil en Entrega] - Canvas interactivo para firma del solicitante al entregar equipos - Conecta con: attachment.service.js y <cd-signature-pad>
class CdSignaturePad extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.isDrawing = false;
    this.lastX = 0;
    this.lastY = 0;
    this.ticketId = null;
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
          backdrop-filter: blur(4px); font-family: inherit;
        }
        .modal {
          background: #ffffff; border-radius: 16px; padding: 24px; width: 440px;
          max-width: 92vw; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
        }
        h3 { margin: 0 0 6px 0; font-size: 1.15rem; color: #0f172a; font-weight: 700; }
        p { margin: 0 0 14px 0; font-size: 0.85rem; color: #64748b; }
        .canvas-box {
          border: 2px dashed #cbd5e1; border-radius: 8px; background: #f8fafc;
          touch-action: none; display: flex; justify-content: center;
        }
        canvas {
          background: #ffffff; border-radius: 6px; cursor: crosshair;
        }
        .actions {
          display: flex; justify-content: space-between; align-items: center; margin-top: 16px;
        }
        .btn-group { display: flex; gap: 8px; }
        button {
          padding: 8px 14px; border-radius: 6px; font-weight: 600; cursor: pointer; border: none; font-size: 0.85rem;
        }
        .btn-clear { background: #f1f5f9; color: #475569; }
        .btn-cancel { background: #e2e8f0; color: #334155; }
        .btn-save { background: #2563eb; color: #ffffff; }
        .btn-save:hover { background: #1d4ed8; }
      </style>
      <div class="overlay">
        <div class="modal">
          <h3>Firma de Conformidad de Entrega</h3>
          <p>Solicita al usuario que firme con el mouse o dedo la recepción del equipo o servicio resuelto.</p>
          <div class="canvas-box">
            <canvas id="sig-canvas" width="380" height="180"></canvas>
          </div>
          <div class="actions">
            <button class="btn-clear" id="btn-clear">Limpiar</button>
            <div class="btn-group">
              <button class="btn-cancel" id="btn-cancel">Cancelar</button>
              <button class="btn-save" id="btn-save">Guardar Firma</button>
            </div>
          </div>
        </div>
      </div>
    `;

    this.attachEvents();
  }

  attachEvents() {
    const canvas = this.shadowRoot.getElementById("sig-canvas");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    ctx.strokeStyle = "#0f172a";
    ctx.lineWidth = 2.5;
    ctx.lineCap = "round";

    const getPos = (e) => {
      const rect = canvas.getBoundingClientRect();
      const clientX = e.touches ? e.touches[0].clientX : e.clientX;
      const clientY = e.touches ? e.touches[0].clientY : e.clientY;
      return {
        x: clientX - rect.left,
        y: clientY - rect.top
      };
    };

    const startDraw = (e) => {
      this.isDrawing = true;
      const pos = getPos(e);
      this.lastX = pos.x;
      this.lastY = pos.y;
    };

    const draw = (e) => {
      if (!this.isDrawing) return;
      e.preventDefault();
      const pos = getPos(e);
      ctx.beginPath();
      ctx.moveTo(this.lastX, this.lastY);
      ctx.lineTo(pos.x, pos.y);
      ctx.stroke();
      this.lastX = pos.x;
      this.lastY = pos.y;
    };

    const stopDraw = () => {
      this.isDrawing = false;
    };

    canvas.addEventListener("mousedown", startDraw);
    canvas.addEventListener("mousemove", draw);
    canvas.addEventListener("mouseup", stopDraw);
    canvas.addEventListener("mouseleave", stopDraw);

    canvas.addEventListener("touchstart", startDraw, { passive: false });
    canvas.addEventListener("touchmove", draw, { passive: false });
    canvas.addEventListener("touchend", stopDraw);

    this.shadowRoot.getElementById("btn-clear")?.addEventListener("click", () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    });

    this.shadowRoot.getElementById("btn-cancel")?.addEventListener("click", () => {
      this.removeAttribute("open");
    });

    this.shadowRoot.getElementById("btn-save")?.addEventListener("click", () => {
      const dataUrl = canvas.toDataURL("image/png");
      this.dispatchEvent(new CustomEvent("signature-saved", {
        bubbles: true,
        composed: true,
        detail: { ticketId: this.ticketId, signatureImage: dataUrl }
      }));
      this.removeAttribute("open");
      window.dispatchEvent(new CustomEvent("app-toast", {
        detail: { type: "success", title: "Firma Registrada", message: "Acta de conformidad firmada exitosamente." }
      }));
    });
  }
}

if (!customElements.get("cd-signature-pad")) {
  customElements.define("cd-signature-pad", CdSignaturePad);
}

export default CdSignaturePad;
