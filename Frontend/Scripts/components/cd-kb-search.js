import {
  searchKbArticles,
  voteKbArticle,
  getKbArticleById
} from "../services/knowledge-base.service.js";

// [Nueva Funcionalidad: U-03/T-09 - Buscador de Base de Conocimiento] - Sugiere artículos resolutivos mientras el usuario redacta o busca ayuda - Conecta con: knowledge-base.service.js y <cd-kb-search>
class CdKbSearch extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.results = [];
    this.selectedArticle = null;
    this.debounceTimer = null;
    this.isLoading = false;
  }

  static get observedAttributes() {
    return ["query", "placeholder"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "query" && newValue) {
      this.doSearch(newValue);
    }
  }

  connectedCallback() {
    this.render();
  }

  onInput(text) {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    if (!text || text.trim().length < 2) {
      this.results = [];
      this.render();
      return;
    }
    this.debounceTimer = setTimeout(() => {
      this.doSearch(text.trim());
    }, 300);
  }

  async doSearch(q) {
    this.isLoading = true;
    this.render();
    try {
      this.results = await searchKbArticles(q);
    } catch (err) {
      console.warn("cd-kb-search: Error searching KB", err);
      this.results = [];
    } finally {
      this.isLoading = false;
      this.render();
    }
  }

  async openArticle(id) {
    try {
      this.selectedArticle = await getKbArticleById(id);
      this.render();
    } catch (err) {
      console.warn("cd-kb-search: Error opening article", err);
    }
  }

  closeModal() {
    this.selectedArticle = null;
    this.render();
  }

  async vote(isHelpful) {
    if (!this.selectedArticle) return;
    try {
      await voteKbArticle(this.selectedArticle.id, isHelpful);
      if (isHelpful) {
        this.selectedArticle.helpfulVotes = (this.selectedArticle.helpfulVotes || 0) + 1;
      } else {
        this.selectedArticle.notHelpfulVotes = (this.selectedArticle.notHelpfulVotes || 0) + 1;
      }
      this.render();
    } catch (err) {
      console.error("cd-kb-search: Error voting", err);
    }
  }

  render() {
    const placeholder = this.getAttribute("placeholder") || "Buscar soluciones en la Base de Conocimiento...";

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          font-family: inherit;
          position: relative;
        }
        .search-box {
          position: relative;
          display: flex;
          align-items: center;
        }
        .search-input {
          width: 100%;
          padding: 10px 14px 10px 38px;
          border: 1px solid #cbd5e1;
          border-radius: 8px;
          font-size: 0.9rem;
          font-family: inherit;
          background: #ffffff;
          box-sizing: border-box;
          transition: border-color 0.15s, box-shadow 0.15s;
        }
        .search-input:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }
        .search-icon {
          position: absolute;
          left: 12px;
          font-size: 1rem;
          color: #94a3b8;
          pointer-events: none;
        }
        .results-dropdown {
          position: absolute;
          top: calc(100% + 4px);
          left: 0;
          right: 0;
          background: #ffffff;
          border-radius: 8px;
          border: 1px solid #e2e8f0;
          box-shadow: 0 10px 20px rgba(0,0,0,0.08);
          max-height: 320px;
          overflow-y: auto;
          z-index: 100;
        }
        .result-item {
          padding: 10px 14px;
          border-bottom: 1px solid #f1f5f9;
          cursor: pointer;
          display: flex;
          flex-direction: column;
          gap: 2px;
          transition: background 0.15s;
        }
        .result-item:hover {
          background: #f8fafc;
        }
        .result-title {
          font-size: 0.875rem;
          font-weight: 600;
          color: #1e293b;
        }
        .result-meta {
          font-size: 0.75rem;
          color: #64748b;
          display: flex;
          gap: 8px;
        }
        .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(15, 23, 42, 0.5);
          backdrop-filter: blur(4px);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 9999;
        }
        .modal-card {
          background: #ffffff;
          border-radius: 14px;
          padding: 24px;
          max-width: 600px;
          width: 90vw;
          max-height: 85vh;
          overflow-y: auto;
          box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
        }
        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;
        }
        .modal-title {
          font-size: 1.15rem;
          font-weight: 700;
          color: #0f172a;
          margin: 0;
        }
        .btn-close {
          background: none;
          border: none;
          font-size: 1.25rem;
          cursor: pointer;
          color: #64748b;
        }
        .modal-content {
          font-size: 0.9rem;
          line-height: 1.6;
          color: #334155;
          margin-bottom: 20px;
          white-space: pre-wrap;
        }
        .modal-footer {
          border-top: 1px solid #e2e8f0;
          padding-top: 14px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 0.85rem;
          color: #64748b;
        }
        .vote-btns {
          display: flex;
          gap: 8px;
        }
        .vote-btn {
          border: 1px solid #cbd5e1;
          background: #ffffff;
          padding: 6px 12px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.8rem;
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .vote-btn:hover {
          background: #f1f5f9;
        }
      </style>

      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" class="search-input" id="search-input" placeholder="${placeholder}" />
      </div>

      ${this.isLoading ? `
        <div class="results-dropdown" style="padding: 12px; text-align: center; color: #64748b; font-size: 0.85rem;">
          Buscando artículos sugeridos...
        </div>
      ` : this.results.length > 0 ? `
        <div class="results-dropdown">
          ${this.results.map(r => `
            <div class="result-item" data-id="${r.id}">
              <span class="result-title">💡 ${r.title}</span>
              <div class="result-meta">
                <span>Categoría: ${r.categoryName || 'General'}</span>
                <span>👍 ${r.helpfulVotes || 0}</span>
                <span>👀 ${r.viewCount || 0} vistas</span>
              </div>
            </div>
          `).join("")}
        </div>
      ` : ""}

      ${this.selectedArticle ? `
        <div class="modal-overlay" id="modal-overlay">
          <div class="modal-card">
            <div class="modal-header">
              <h3 class="modal-title">${this.selectedArticle.title}</h3>
              <button class="btn-close" id="btn-close-modal">✕</button>
            </div>
            <div class="modal-content">${this.selectedArticle.content}</div>
            <div class="modal-footer">
              <span>¿Te resultó útil esta respuesta?</span>
              <div class="vote-btns">
                <button class="vote-btn" id="vote-yes">👍 Sí (${this.selectedArticle.helpfulVotes || 0})</button>
                <button class="vote-btn" id="vote-no">👎 No (${this.selectedArticle.notHelpfulVotes || 0})</button>
              </div>
            </div>
          </div>
        </div>
      ` : ""}
    `;

    this.attachEvents();
  }

  attachEvents() {
    const input = this.shadowRoot.getElementById("search-input");
    if (input) {
      input.addEventListener("input", (e) => this.onInput(e.target.value));
    }

    this.shadowRoot.querySelectorAll(".result-item").forEach(item => {
      item.addEventListener("click", () => {
        const id = item.getAttribute("data-id");
        if (id) this.openArticle(id);
      });
    });

    this.shadowRoot.getElementById("btn-close-modal")?.addEventListener("click", () => {
      this.closeModal();
    });

    this.shadowRoot.getElementById("modal-overlay")?.addEventListener("click", (e) => {
      if (e.target.id === "modal-overlay") this.closeModal();
    });

    this.shadowRoot.getElementById("vote-yes")?.addEventListener("click", () => {
      this.vote(true);
    });

    this.shadowRoot.getElementById("vote-no")?.addEventListener("click", () => {
      this.vote(false);
    });
  }
}

if (!customElements.get("cd-kb-search")) {
  customElements.define("cd-kb-search", CdKbSearch);
}

export default CdKbSearch;
