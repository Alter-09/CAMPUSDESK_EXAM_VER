import { getActiveAnnouncements } from "../services/announcement.service.js";

// [Nueva Funcionalidad: U-27/A-09 - Banner de Anuncios y Mantenimientos] - Alerta globalmente a usuarios y técnicos sobre interrupciones o comunicados - Conecta con: announcement.service.js y <cd-announcement-banner>
class CdAnnouncementBanner extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.announcements = [];
    this.currentIndex = 0;
    this.dismissedIds = new Set(JSON.parse(sessionStorage.getItem("dismissed_announcements") || "[]"));
  }

  connectedCallback() {
    this.loadAnnouncements();
  }

  async loadAnnouncements() {
    try {
      const all = await getActiveAnnouncements();
      this.announcements = (Array.isArray(all) ? all : []).filter(a => !this.dismissedIds.has(a.id));
      this.render();
    } catch (err) {
      console.warn("cd-announcement-banner: Error loading active announcements", err);
    }
  }

  dismiss(id) {
    this.dismissedIds.add(id);
    sessionStorage.setItem("dismissed_announcements", JSON.stringify([...this.dismissedIds]));
    this.announcements = this.announcements.filter(a => a.id !== id);
    if (this.currentIndex >= this.announcements.length) {
      this.currentIndex = 0;
    }
    this.render();
  }

  render() {
    if (!this.announcements || this.announcements.length === 0) {
      this.shadowRoot.innerHTML = "";
      return;
    }

    const current = this.announcements[this.currentIndex];
    const isMaintenance = current.type === "MAINTENANCE" || current.type === "WARNING";
    const isOutage = current.type === "OUTAGE" || current.type === "DANGER" || current.type === "CRITICAL";

    let themeClass = "theme-info";
    let icon = "ℹ️";
    if (isOutage) {
      themeClass = "theme-danger";
      icon = "🚨";
    } else if (isMaintenance) {
      themeClass = "theme-warning";
      icon = "⚠️";
    }

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          width: 100%;
          font-family: inherit;
          margin-bottom: 16px;
        }
        .banner {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 12px 18px;
          border-radius: 10px;
          font-size: 0.875rem;
          box-shadow: 0 2px 4px rgba(0,0,0,0.04);
          animation: slideDown 0.3s ease-out;
        }
        @keyframes slideDown {
          from { opacity: 0; transform: translateY(-8px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .theme-info {
          background: #eff6ff;
          border: 1px solid #bfdbfe;
          color: #1e40af;
        }
        .theme-warning {
          background: #fffbeb;
          border: 1px solid #fde68a;
          color: #92400e;
        }
        .theme-danger {
          background: #fef2f2;
          border: 1px solid #fecaca;
          color: #991b1b;
        }
        .content {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;
        }
        .icon {
          font-size: 1.25rem;
        }
        .message-wrap {
          display: flex;
          flex-direction: column;
        }
        .title {
          font-weight: 700;
          margin-bottom: 2px;
        }
        .body {
          opacity: 0.95;
        }
        .actions {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .nav-btn, .close-btn {
          background: none;
          border: none;
          cursor: pointer;
          color: inherit;
          padding: 4px 6px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0.8;
          font-size: 0.9rem;
        }
        .nav-btn:hover, .close-btn:hover {
          opacity: 1;
          background: rgba(0,0,0,0.06);
        }
        .counter {
          font-size: 0.75rem;
          opacity: 0.8;
        }
      </style>

      <div class="banner ${themeClass}">
        <div class="content">
          <span class="icon">${icon}</span>
          <div class="message-wrap">
            <span class="title">${current.title}</span>
            <span class="body">${current.content || current.message || ""}</span>
          </div>
        </div>
        <div class="actions">
          ${this.announcements.length > 1 ? `
            <span class="counter">${this.currentIndex + 1}/${this.announcements.length}</span>
            <button class="nav-btn" id="btn-prev" title="Anterior">◀</button>
            <button class="nav-btn" id="btn-next" title="Siguiente">▶</button>
          ` : ""}
          <button class="close-btn" id="btn-dismiss" title="Cerrar comunicado">✕</button>
        </div>
      </div>
    `;

    this.attachEvents(current.id);
  }

  attachEvents(currentId) {
    this.shadowRoot.getElementById("btn-dismiss")?.addEventListener("click", () => {
      this.dismiss(currentId);
    });

    this.shadowRoot.getElementById("btn-prev")?.addEventListener("click", () => {
      this.currentIndex = (this.currentIndex - 1 + this.announcements.length) % this.announcements.length;
      this.render();
    });

    this.shadowRoot.getElementById("btn-next")?.addEventListener("click", () => {
      this.currentIndex = (this.currentIndex + 1) % this.announcements.length;
      this.render();
    });
  }
}

if (!customElements.get("cd-announcement-banner")) {
  customElements.define("cd-announcement-banner", CdAnnouncementBanner);
}

export default CdAnnouncementBanner;
