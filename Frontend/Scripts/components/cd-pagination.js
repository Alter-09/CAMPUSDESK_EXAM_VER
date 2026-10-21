// [Nueva Funcionalidad: U-29/T-29/A-30 - Control de Paginación Reactivo] - Provee barra de navegación por páginas sincronizada con backend - Conecta con: TicketService/PageResponse y <cd-pagination>
class CdPagination extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.page = 0;
    this.size = 10;
    this.totalElements = 0;
    this.totalPages = 1;
  }

  static get observedAttributes() {
    return ["page", "size", "total-elements", "total-pages"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue) return;
    if (name === "page") this.page = parseInt(newValue, 10) || 0;
    if (name === "size") this.size = parseInt(newValue, 10) || 10;
    if (name === "total-elements") this.totalElements = parseInt(newValue, 10) || 0;
    if (name === "total-pages") this.totalPages = parseInt(newValue, 10) || 1;
    this.render();
  }

  connectedCallback() {
    this.render();
  }

  goToPage(p) {
    if (p < 0 || p >= this.totalPages || p === this.page) return;
    this.page = p;
    this.dispatchEvent(new CustomEvent("page-change", {
      bubbles: true,
      composed: true,
      detail: { page: this.page, size: this.size }
    }));
  }

  changeSize(newSize) {
    this.size = newSize;
    this.page = 0;
    this.dispatchEvent(new CustomEvent("page-change", {
      bubbles: true,
      composed: true,
      detail: { page: this.page, size: this.size }
    }));
  }

  render() {
    const startItem = this.totalElements === 0 ? 0 : this.page * this.size + 1;
    const endItem = Math.min((this.page + 1) * this.size, this.totalElements);

    // Compute visible page windows (max 5 page buttons)
    const pages = [];
    const maxBtns = 5;
    let startPage = Math.max(0, this.page - Math.floor(maxBtns / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxBtns - 1);
    if (endPage - startPage < maxBtns - 1) {
      startPage = Math.max(0, endPage - maxBtns + 1);
    }
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          font-family: inherit;
          margin-top: 16px;
        }
        .pagination-container {
          display: flex;
          align-items: center;
          justify-content: space-between;
          flex-wrap: wrap;
          gap: 12px;
          padding: 10px 16px;
          background: #ffffff;
          border-radius: 10px;
          border: 1px solid #e2e8f0;
          font-size: 0.85rem;
          color: #64748b;
        }
        .info {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .info b {
          color: #0f172a;
        }
        .size-select {
          border: 1px solid #cbd5e1;
          border-radius: 6px;
          padding: 4px 8px;
          font-size: 0.85rem;
          color: #1e293b;
          background: white;
          cursor: pointer;
        }
        .controls {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .btn {
          border: 1px solid #e2e8f0;
          background: #ffffff;
          color: #334155;
          min-width: 32px;
          height: 32px;
          padding: 0 8px;
          border-radius: 6px;
          font-size: 0.85rem;
          font-weight: 500;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          transition: all 0.15s;
        }
        .btn:hover:not(:disabled) {
          background: #f1f5f9;
          border-color: #cbd5e1;
          color: #0f172a;
        }
        .btn:disabled {
          opacity: 0.4;
          cursor: not-allowed;
        }
        .btn.active {
          background: #2563eb;
          border-color: #2563eb;
          color: #ffffff;
          font-weight: 600;
        }
      </style>

      <div class="pagination-container">
        <div class="info">
          <span>Mostrando <b>${startItem}</b> a <b>${endItem}</b> de <b>${this.totalElements}</b></span>
          <span>·</span>
          <span>Por página:</span>
          <select class="size-select" id="size-select">
            <option value="10" ${this.size === 10 ? "selected" : ""}>10</option>
            <option value="20" ${this.size === 20 ? "selected" : ""}>20</option>
            <option value="50" ${this.size === 50 ? "selected" : ""}>50</option>
          </select>
        </div>

        <div class="controls">
          <button class="btn" id="btn-first" ${this.page === 0 ? "disabled" : ""} title="Primera página">«</button>
          <button class="btn" id="btn-prev" ${this.page === 0 ? "disabled" : ""} title="Página anterior">‹</button>

          ${pages.map(p => `
            <button class="btn ${p === this.page ? "active" : ""}" data-page="${p}">${p + 1}</button>
          `).join("")}

          <button class="btn" id="btn-next" ${this.page >= this.totalPages - 1 ? "disabled" : ""} title="Página siguiente">›</button>
          <button class="btn" id="btn-last" ${this.page >= this.totalPages - 1 ? "disabled" : ""} title="Última página">»</button>
        </div>
      </div>
    `;

    this.attachEvents();
  }

  attachEvents() {
    this.shadowRoot.getElementById("size-select")?.addEventListener("change", (e) => {
      this.changeSize(parseInt(e.target.value, 10));
    });

    this.shadowRoot.getElementById("btn-first")?.addEventListener("click", () => this.goToPage(0));
    this.shadowRoot.getElementById("btn-prev")?.addEventListener("click", () => this.goToPage(this.page - 1));
    this.shadowRoot.getElementById("btn-next")?.addEventListener("click", () => this.goToPage(this.page + 1));
    this.shadowRoot.getElementById("btn-last")?.addEventListener("click", () => this.goToPage(this.totalPages - 1));

    this.shadowRoot.querySelectorAll(".btn[data-page]").forEach(btn => {
      btn.addEventListener("click", () => {
        const p = parseInt(btn.getAttribute("data-page"), 10);
        this.goToPage(p);
      });
    });
  }
}

if (!customElements.get("cd-pagination")) {
  customElements.define("cd-pagination", CdPagination);
}

export default CdPagination;
