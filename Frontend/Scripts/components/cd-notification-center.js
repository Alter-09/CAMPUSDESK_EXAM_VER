import {
  getMyNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from "../services/notification.service.js";

// [Nueva Funcionalidad: U-02/T-04 - Centro de Notificaciones] - Muestra campana con badge y lista desplegable de alertas - Conecta con: notification.service.js y <cd-notification-center>
class CdNotificationCenter extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.notifications = [];
    this.isOpen = false;
    this.pollInterval = null;
  }

  connectedCallback() {
    this.render();
    this.loadNotifications();
    this.pollInterval = setInterval(() => this.loadNotifications(), 60000);
  }

  disconnectedCallback() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  async loadNotifications() {
    try {
      this.notifications = await getMyNotifications();
      this.render();
    } catch (err) {
      console.warn("cd-notification-center: Error loading notifications", err);
    }
  }

  async markAllRead() {
    try {
      await markAllNotificationsAsRead();
      this.notifications = this.notifications.map(n => ({ ...n, read: true }));
      this.render();
    } catch (err) {
      console.error("cd-notification-center: Error marking all read", err);
    }
  }

  async markRead(id) {
    try {
      await markNotificationAsRead(id);
      this.notifications = this.notifications.map(n => n.id === id ? { ...n, read: true } : n);
      this.render();
    } catch (err) {
      console.error("cd-notification-center: Error marking read", err);
    }
  }

  toggleDropdown() {
    this.isOpen = !this.isOpen;
    this.render();
  }

  formatDate(isoStr) {
    if (!isoStr) return "";
    try {
      const d = new Date(isoStr);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + " · " + d.toLocaleDateString([], { month: 'short', day: 'numeric' });
    } catch {
      return isoStr;
    }
  }

  render() {
    const unreadCount = this.notifications.filter(n => !n.read).length;

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: inline-block;
          position: relative;
          font-family: inherit;
        }
        .bell-btn {
          background: none;
          border: none;
          position: relative;
          cursor: pointer;
          padding: 8px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #64748b;
          transition: background 0.15s, color 0.15s;
        }
        .bell-btn:hover {
          background: #f1f5f9;
          color: #0f172a;
        }
        .bell-icon {
          width: 22px;
          height: 22px;
          fill: currentColor;
        }
        .badge {
          position: absolute;
          top: 4px;
          right: 4px;
          background: #ef4444;
          color: white;
          font-size: 0.7rem;
          font-weight: 700;
          min-width: 18px;
          height: 18px;
          border-radius: 999px;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 0 4px;
        }
        .dropdown {
          position: absolute;
          right: 0;
          top: calc(100% + 8px);
          width: 340px;
          max-width: 90vw;
          background: white;
          border-radius: 12px;
          box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1);
          border: 1px solid #e2e8f0;
          z-index: 1000;
          overflow: hidden;
          display: flex;
          flex-direction: column;
        }
        .header {
          padding: 12px 16px;
          border-bottom: 1px solid #f1f5f9;
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
        .header h4 {
          margin: 0;
          font-size: 0.95rem;
          color: #0f172a;
        }
        .mark-all-btn {
          background: none;
          border: none;
          color: #2563eb;
          font-size: 0.775rem;
          font-weight: 600;
          cursor: pointer;
        }
        .mark-all-btn:hover {
          text-decoration: underline;
        }
        .list {
          max-height: 350px;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
        }
        .item {
          padding: 12px 16px;
          border-bottom: 1px solid #f8fafc;
          cursor: pointer;
          transition: background 0.15s;
          display: flex;
          flex-direction: column;
          gap: 4px;
        }
        .item:hover {
          background: #f8fafc;
        }
        .item.unread {
          background: #eff6ff;
        }
        .item.unread:hover {
          background: #dbeafe;
        }
        .item-top {
          display: flex;
          justify-content: space-between;
          font-size: 0.75rem;
          color: #64748b;
        }
        .item-title {
          font-size: 0.85rem;
          font-weight: 600;
          color: #1e293b;
        }
        .item-desc {
          font-size: 0.8rem;
          color: #475569;
          margin: 0;
          line-height: 1.3;
        }
        .empty {
          padding: 30px 16px;
          text-align: center;
          color: #94a3b8;
          font-size: 0.85rem;
        }
      </style>

      <button class="bell-btn" id="btn-toggle" title="Notificaciones" aria-label="Notificaciones">
        <svg class="bell-icon" viewBox="0 0 24 24">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
        </svg>
        ${unreadCount > 0 ? `<span class="badge">${unreadCount}</span>` : ""}
      </button>

      ${this.isOpen ? `
        <div class="dropdown">
          <div class="header">
            <h4>Notificaciones</h4>
            ${unreadCount > 0 ? `<button class="mark-all-btn" id="btn-mark-all">Marcar leídas</button>` : ""}
          </div>
          <div class="list">
            ${this.notifications.length === 0 ? `
              <div class="empty">No tienes notificaciones pendientes</div>
            ` : this.notifications.map(n => `
              <div class="item ${n.read ? '' : 'unread'}" data-id="${n.id}">
                <div class="item-top">
                  <span>${n.channel || 'IN_APP'}</span>
                  <span>${this.formatDate(n.createdAt)}</span>
                </div>
                <div class="item-title">${n.title || 'Aviso de soporte'}</div>
                <p class="item-desc">${n.message || ''}</p>
              </div>
            `).join('')}
          </div>
        </div>
      ` : ''}
    `;

    this.attachEvents();
  }

  attachEvents() {
    this.shadowRoot.getElementById("btn-toggle")?.addEventListener("click", (e) => {
      e.stopPropagation();
      this.toggleDropdown();
    });

    this.shadowRoot.getElementById("btn-mark-all")?.addEventListener("click", (e) => {
      e.stopPropagation();
      this.markAllRead();
    });

    this.shadowRoot.querySelectorAll(".item").forEach(itemEl => {
      itemEl.addEventListener("click", (e) => {
        const id = itemEl.getAttribute("data-id");
        if (id) {
          this.markRead(id);
          const notification = this.notifications.find(n => String(n.id) === String(id));
          this.dispatchEvent(new CustomEvent("notification-selected", {
            bubbles: true,
            composed: true,
            detail: notification
          }));
        }
      });
    });
  }
}

if (!customElements.get("cd-notification-center")) {
  customElements.define("cd-notification-center", CdNotificationCenter);
}

export default CdNotificationCenter;
