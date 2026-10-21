import { apiRequest } from "../config/http-client.js";

const SVG_NS = "http://www.w3.org/2000/svg";
const VIEWBOX = { width: 680, height: 320, top: 30, right: 30, bottom: 270, left: 45 };
const MONTH_LABELS = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
const MONTH_NAMES = [
  "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
  "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

function createSvg(tag, attrs = {}) {
  const el = document.createElementNS(SVG_NS, tag);
  for (const [key, val] of Object.entries(attrs)) {
    el.setAttribute(key, String(val));
  }
  return el;
}

function getCurvedPath(points) {
  if (points.length === 0) return "";
  if (points.length === 1) return `M ${points[0].x.toFixed(1)} ${points[0].y.toFixed(1)}`;

  let d = `M ${points[0].x.toFixed(1)} ${points[0].y.toFixed(1)}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = i === 0 ? points[i] : points[i - 1];
    const p1 = points[i];
    const p2 = points[i + 1];
    const p3 = i + 2 < points.length ? points[i + 2] : points[i + 1];

    const cp1x = p1.x + (p2.x - p0.x) / 6;
    const cp1y = p1.y + (p2.y - p0.y) / 6;
    const cp2x = p2.x - (p3.x - p1.x) / 6;
    const cp2y = p2.y - (p3.y - p1.y) / 6;

    d += ` C ${cp1x.toFixed(1)} ${cp1y.toFixed(1)}, ${cp2x.toFixed(1)} ${cp2y.toFixed(1)}, ${p2.x.toFixed(1)} ${p2.y.toFixed(1)}`;
  }
  return d;
}

function getCurvedAreaPath(points, bottomY) {
  if (points.length === 0) return "";
  const linePath = getCurvedPath(points);
  const first = points[0];
  const last = points[points.length - 1];
  return `${linePath} L ${last.x.toFixed(1)} ${bottomY.toFixed(1)} L ${first.x.toFixed(1)} ${bottomY.toFixed(1)} Z`;
}

class UserDashboard extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.stats = null;
    this.tickets = [];
    this.monthlyData = [];
    this.pointsCyan = [];
    this.pointsPurple = [];
    this.activeIndex = new Date().getMonth();
    this.loading = true;
  }

  async connectedCallback() {
    this.renderLoading();
    await this.loadData();
  }

  async loadData() {
    try {
      const [statsResult, ticketsResult] = await Promise.allSettled([
        apiRequest("/api/dashboard/stats"),
        apiRequest("/api/tickets"),
      ]);

      this.stats = statsResult.status === "fulfilled" && statsResult.value
        ? statsResult.value
        : {
            totalTickets: 0,
            openTickets: 0,
            assignedTickets: 0,
            inProgressTickets: 0,
            resolvedTickets: 0,
            closedTickets: 0,
          };

      this.tickets = ticketsResult.status === "fulfilled" && Array.isArray(ticketsResult.value)
        ? ticketsResult.value
        : [];

      this.processData();
      this.loading = false;
      this.render();
      this.initChart();
    } catch (err) {
      console.error("Error al cargar dashboard de usuario:", err);
      this.loading = false;
      this.renderError();
    }
  }

  processData() {
    const total = Number(this.stats?.totalTickets ?? this.tickets.length);
    const open = Number(this.stats?.openTickets ?? 0);
    const assigned = Number(this.stats?.assignedTickets ?? 0);
    const inProgress = Number(this.stats?.inProgressTickets ?? 0);
    const resolved = Number(this.stats?.resolvedTickets ?? 0);
    const closed = Number(this.stats?.closedTickets ?? 0);

    const activeTotal = open + assigned + inProgress;
    const completedTotal = resolved + closed;

    // Initialize 12 months strictly to zero (no simulated data)
    const buckets = Array.from({ length: 12 }, (_, idx) => ({
      index: idx,
      shortLabel: MONTH_LABELS[idx],
      fullLabel: MONTH_NAMES[idx],
      cyan: 0,   // Solicitudes abiertas o en curso
      purple: 0, // Solicitudes resueltas o cerradas
      total: 0,
      growth: "0.0%",
      efficiency: "0%",
    }));

    // Group real tickets retrieved from PostgreSQL by creation month
    let hasRealTicketDates = false;
    if (Array.isArray(this.tickets) && this.tickets.length > 0) {
      this.tickets.forEach((ticket) => {
        const rawDate = ticket.createdAt || ticket.creationDate;
        if (rawDate) {
          const d = new Date(rawDate);
          if (!isNaN(d.getTime())) {
            const m = d.getMonth();
            if (m >= 0 && m < 12) {
              hasRealTicketDates = true;
              const isResolved = ticket.status === "RESOLVED" || ticket.status === "CLOSED";
              if (isResolved) {
                buckets[m].purple += 1;
              } else {
                buckets[m].cyan += 1;
              }
            }
          }
        }
      });
    }

    // If no individual dates exist but metrics API reports requests,
    // reflect them accurately in current system month
    const currentMonth = new Date().getMonth();
    if (!hasRealTicketDates && total > 0) {
      buckets[currentMonth].cyan = activeTotal;
      buckets[currentMonth].purple = completedTotal;
    }

    // Compute month-over-month real metrics based 100% on real data
    let prevTotal = 0;
    this.monthlyData = buckets.map((b, idx) => {
      const mTotal = b.cyan + b.purple;
      b.total = mTotal;
      const eff = mTotal > 0 ? ((b.purple / mTotal) * 100).toFixed(0) : "0";
      b.efficiency = `${eff}%`;

      let growth = "0.0%";
      if (idx > 0) {
        if (prevTotal === 0 && mTotal > 0) {
          growth = `+${mTotal * 100}%`;
        } else if (prevTotal > 0) {
          const diff = ((mTotal - prevTotal) / prevTotal) * 100;
          growth = diff >= 0 ? `+${diff.toFixed(1)}%` : `${diff.toFixed(1)}%`;
        }
      }
      b.growth = growth;
      prevTotal = mTotal;
      return b;
    });

    this.activeIndex = currentMonth;
  }

  renderLoading() {
    this.shadowRoot.innerHTML = `
      <style>
        :host { display: block; color: #cbd5e1; font-family: var(--cd-font-body, sans-serif); }
        .loading-wrap { padding: 3rem; text-align: center; color: #94a3b8; }
      </style>
      <div class="loading-wrap">Cargando tu panel personal de soporte…</div>
    `;
  }

  renderError() {
    this.shadowRoot.innerHTML = `
      <style>
        :host { display: block; color: #f87171; font-family: var(--cd-font-body, sans-serif); }
        .error-wrap { padding: 2rem; background: rgba(239, 68, 68, 0.1); border-radius: 12px; border: 1px solid rgba(239,68,68,0.2); }
      </style>
      <div class="error-wrap">No se pudieron sincronizar tus solicitudes con el servidor.</div>
    `;
  }

  render() {
    const total = Number(this.stats?.totalTickets ?? this.tickets.length);
    const open = Number(this.stats?.openTickets ?? 0);
    const assigned = Number(this.stats?.assignedTickets ?? 0);
    const inProgress = Number(this.stats?.inProgressTickets ?? 0);
    const resolved = Number(this.stats?.resolvedTickets ?? 0);
    const closed = Number(this.stats?.closedTickets ?? 0);

    const active = open + assigned + inProgress;
    const completed = resolved + closed;
    const efficiency = total > 0 ? Math.round((completed / total) * 100) : 0;

    const pctOpen = total > 0 ? Math.min(100, Math.round((open / total) * 100)) : 0;
    const pctActive = total > 0 ? Math.min(100, Math.round(((assigned + inProgress) / total) * 100)) : 0;
    const pctCompleted = total > 0 ? Math.min(100, Math.round((completed / total) * 100)) : 0;

    // Retrieve recent requests of the user
    const recentTickets = [...this.tickets]
      .sort((a, b) => (Number(b.id) || 0) - (Number(a.id) || 0))
      .slice(0, 4);

    const ticketListHtml = recentTickets.length > 0
      ? recentTickets.map((t) => {
          let tagClass = "priority-queue-item__tag--status";
          let labelText = "ABIERTO";

          if (t.status === "ASSIGNED") {
            labelText = "ASIGNADO";
          } else if (t.status === "IN_PROGRESS") {
            labelText = "EN PROCESO";
          } else if (t.status === "RESOLVED") {
            tagClass = "priority-queue-item__tag--resolved";
            labelText = "RESUELTO";
          } else if (t.status === "CLOSED") {
            tagClass = "priority-queue-item__tag--resolved";
            labelText = "CERRADO";
          }

          return `
            <a href="./my-tickets.html" class="priority-queue-item" title="Ver mis solicitudes">
              <div class="priority-queue-item__icon priority-queue-item__icon--medium">
                📄
              </div>
              <div class="priority-queue-item__content">
                <p class="priority-queue-item__title">#${t.id} — ${t.title || 'Solicitud sin título'}</p>
                <div class="priority-queue-item__meta">
                  <span class="priority-queue-item__tag">${t.category || 'SOPORTE'}</span>
                  <span class="priority-queue-item__tag ${tagClass}">${labelText}</span>
                  <span style="color:#94a3b8;">Prioridad: <strong>${t.priority || 'NORMAL'}</strong></span>
                </div>
              </div>
            </a>
          `;
        }).join("")
      : `<div class="empty-queue-msg">Aún no has creado solicitudes de soporte.</div>`;

    this.shadowRoot.innerHTML = `
      <style>
        @import url("./stylesheet/tokens.css");
        @import url("./stylesheet/components/role-dashboard.css");
        @import url("./stylesheet/components/revenue-chart.css");
        :host { display: block; width: 100%; }
      </style>

      <div class="role-dashboard">
        <!-- Cabecera de Bienvenida del Usuario -->
        <header class="role-dashboard__header">
          <div>
            <h1 class="role-dashboard__title">Panel de Mis Solicitudes</h1>
            <p class="role-dashboard__subtitle">Historial de incidencias, seguimiento de atención en tiempo real y métricas de resolución.</p>
          </div>
          <div>
            <span class="role-dashboard__badge-role">
              <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#34d399; box-shadow:0 0 8px #34d399;"></span>
              Usuario Solicitante
            </span>
          </div>
        </header>

        <!-- Fila de 4 Tarjetas KPI con barras de progreso (Fortify360 Style) -->
        <section class="role-kpi-grid" aria-label="Indicadores del usuario">
          <!-- Card 1: Total Reportadas -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--cyan">Historial</span>
            </div>
            <p class="role-kpi-card__label">Total Solicitudes Creadas</p>
            <strong class="role-kpi-card__value">${total}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--cyan" style="width: 100%;"></div>
              </div>
              <span class="role-kpi-card__percent">100%</span>
            </div>
          </article>

          <!-- Card 2: En Espera (Abiertas) -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--amber">En Espera</span>
            </div>
            <p class="role-kpi-card__label">Esperando Asignación</p>
            <strong class="role-kpi-card__value">${open}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--amber" style="width: ${pctOpen}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctOpen}%</span>
            </div>
          </article>

          <!-- Card 3: En Atención Activa -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--cyan">En Curso</span>
            </div>
            <p class="role-kpi-card__label">En Atención Técnica</p>
            <strong class="role-kpi-card__value">${assigned + inProgress}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--cyan" style="width: ${pctActive}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctActive}%</span>
            </div>
          </article>

          <!-- Card 4: Solucionadas & Cerradas -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--green">Completadas</span>
            </div>
            <p class="role-kpi-card__label">Solucionadas & Cerradas</p>
            <strong class="role-kpi-card__value">${completed}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--green" style="width: ${pctCompleted}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctCompleted}%</span>
            </div>
          </article>
        </section>

        <!-- Grid de 2 Columnas: Gráfico Luminiscente de Mis Casos + Lista & Acción Rápida -->
        <section class="role-dashboard-grid">
          <!-- Columna Izquierda: Curva de Solicitudes Personales -->
          <div class="revenue-chart">
            <div class="revenue-chart__header">
              <div class="revenue-chart__heading">
                <p class="revenue-chart__eyebrow">Flujo de Mis Solicitudes</p>
                <div class="revenue-chart__headline">
                  <span class="revenue-chart__main-val">${completed}</span>
                  <span class="revenue-chart__trend-badge">+${efficiency}% Éxito</span>
                </div>
              </div>
              <div class="revenue-chart__header-right">
                <div class="revenue-chart__legend">
                  <div class="revenue-chart__legend-item">
                    <span class="revenue-chart__legend-dot revenue-chart__legend-dot--purple"></span>
                    <span class="revenue-chart__legend-label">Solucionadas</span>
                  </div>
                  <div class="revenue-chart__legend-item">
                    <span class="revenue-chart__legend-dot revenue-chart__legend-dot--cyan"></span>
                    <span class="revenue-chart__legend-label">En Atención</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="revenue-chart__plot" id="user-plot">
              <svg class="revenue-chart__svg" id="user-svg" viewBox="0 0 680 320" role="img" aria-label="Curva de solicitudes de usuario"></svg>
              
              <div class="revenue-chart__tooltip" id="user-tooltip" role="status" aria-live="polite">
                <p class="revenue-chart__tooltip-date" id="utt-date">Octubre 2026</p>
                <div class="revenue-chart__tooltip-top">
                  <strong class="revenue-chart__tooltip-total" id="utt-total">0 solicitudes</strong>
                  <span class="revenue-chart__tooltip-growth" id="utt-badge">0.0%</span>
                </div>
                <div class="revenue-chart__tooltip-divider"></div>
                <div class="revenue-chart__tooltip-row">
                  <span class="tooltip-dot tooltip-dot--purple"></span>
                  <span class="tooltip-name">Solucionadas:</span>
                  <strong id="utt-resolved">0</strong>
                </div>
                <div class="revenue-chart__tooltip-row">
                  <span class="tooltip-dot tooltip-dot--cyan"></span>
                  <span class="tooltip-name">En curso:</span>
                  <strong id="utt-active">0</strong>
                </div>
              </div>
            </div>

            <div class="revenue-chart__footer">
              <span class="revenue-chart__source">Sincronizado en tiempo real con CampusDesk</span>
            </div>
          </div>

          <!-- Columna Derecha: Mis Solicitudes Recientes + Acción Rápida -->
          <div style="display:flex; flex-direction:column; gap:1.25rem;">
            <div class="priority-queue-card">
              <div class="priority-queue-card__header">
                <div>
                  <h2 class="priority-queue-card__title">Mis Solicitudes Recientes</h2>
                  <p class="priority-queue-card__subtitle">Estado de tus últimos casos reportados</p>
                </div>
                <span class="priority-queue-card__badge-count">${recentTickets.length} recientes</span>
              </div>

              <div class="priority-queue-list">
                ${ticketListHtml}
              </div>

              <a href="./my-tickets.html" class="quick-action-card__btn quick-action-card__btn--outline" style="margin-top:auto;">
                <span>Ver Todas Mis Solicitudes</span>
                <span>→</span>
              </a>
            </div>

            <!-- Tarjeta de Acción Rápida -->
            <div class="quick-action-card">
              <h2 class="quick-action-card__title">
                <span></span>
                <span>¿Tienes una nueva incidencia?</span>
              </h2>
              <p class="quick-action-card__desc">
                Reporta fallas de hardware, problemas de software, conectividad o acceso a sistemas en pocos pasos.
              </p>
              <a href="./create-ticket.html" class="quick-action-card__btn">
                <span>+ Crear Nueva Solicitud</span>
              </a>
            </div>
          </div>
        </section>
      </div>
    `;
  }

  initChart() {
    const svg = this.shadowRoot.getElementById("user-svg");
    const plot = this.shadowRoot.getElementById("user-plot");
    const tooltip = this.shadowRoot.getElementById("user-tooltip");

    if (!svg || !plot || !tooltip) return;

    svg.replaceChildren();

    const defs = createSvg("defs");
    const cyanGrad = createSvg("linearGradient", { id: "userCyanArea", x1: "0", y1: "0", x2: "0", y2: "1" });
    cyanGrad.append(
      createSvg("stop", { offset: "0%", "stop-color": "#22d3ee", "stop-opacity": "0.35" }),
      createSvg("stop", { offset: "50%", "stop-color": "#0284c7", "stop-opacity": "0.12" }),
      createSvg("stop", { offset: "100%", "stop-color": "#070f24", "stop-opacity": "0.0" })
    );
    defs.append(cyanGrad);
    svg.append(defs);

    const bounds = {
      left: VIEWBOX.left,
      right: VIEWBOX.width - VIEWBOX.right,
      top: VIEWBOX.top,
      bottom: VIEWBOX.bottom,
    };

    const allValues = [
      ...this.monthlyData.map((d) => d.cyan),
      ...this.monthlyData.map((d) => d.purple),
    ];
    const maxVal = Math.max(...allValues, 2);
    const ceiling = Math.max(4, Math.ceil((maxVal * 1.2) / 2) * 2);

    const toY = (val) => bounds.bottom - (val / ceiling) * (bounds.bottom - bounds.top);
    const stepX = (bounds.right - bounds.left) / (this.monthlyData.length - 1);
    const toX = (idx) => bounds.left + idx * stepX;

    // Grid horizontal
    const gridGroup = createSvg("g");
    const steps = 3;
    for (let i = 0; i <= steps; i++) {
      const val = Math.round((ceiling / steps) * i);
      const y = toY(val);

      gridGroup.append(
        createSvg("line", {
          class: "revenue-chart__grid-line",
          x1: bounds.left,
          y1: y,
          x2: bounds.right,
          y2: y,
        })
      );

      const text = createSvg("text", {
        class: "revenue-chart__axis-text revenue-chart__axis-text--y",
        x: bounds.left - 10,
        y: y + 4,
      });
      text.textContent = String(val);
      gridGroup.append(text);
    }
    svg.append(gridGroup);

    this.pointsCyan = this.monthlyData.map((d, i) => ({ x: toX(i), y: toY(d.cyan), data: d }));
    this.pointsPurple = this.monthlyData.map((d, i) => ({ x: toX(i), y: toY(d.purple), data: d }));
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-area",
        d: getCurvedAreaPath(this.pointsCyan, bounds.bottom),
        fill: "url(#userCyanArea)",
      })
    );
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--purple",
        d: getCurvedPath(this.pointsPurple),
      })
    );
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--cyan",
        d: getCurvedPath(this.pointsCyan),
      })
    );

    const activeIdx = Math.min(this.activeIndex, this.pointsCyan.length - 1);
    const pC = this.pointsCyan[activeIdx];
    const pP = this.pointsPurple[activeIdx];

    svg.append(
      createSvg("circle", { cx: pC.x, cy: pC.y, r: 4, fill: "#22d3ee", stroke: "#070f24", "stroke-width": 2 }),
      createSvg("circle", { cx: pP.x, cy: pP.y, r: 4, fill: "#c084fc", stroke: "#070f24", "stroke-width": 2 })
    );

    const xGroup = createSvg("g");
    this.monthlyData.forEach((d, i) => {
      const text = createSvg("text", {
        class: `revenue-chart__axis-text revenue-chart__axis-text--x ${i === activeIdx ? "is-active" : ""}`,
        x: toX(i),
        y: bounds.bottom + 20,
      });
      text.textContent = d.shortLabel;
      xGroup.append(text);
    });
    svg.append(xGroup);

    const showTooltip = (idx) => {
      const item = this.monthlyData[idx];
      const ptC = this.pointsCyan[idx];
      const ptP = this.pointsPurple[idx];
      if (!item || !ptC) return;

      const ttDate = this.shadowRoot.getElementById("utt-date");
      const ttTotal = this.shadowRoot.getElementById("utt-total");
      const ttBadge = this.shadowRoot.getElementById("utt-badge");
      const ttResolved = this.shadowRoot.getElementById("utt-resolved");
      const ttActive = this.shadowRoot.getElementById("utt-active");

      if (ttDate) ttDate.textContent = `${item.fullLabel} ${new Date().getFullYear()}`;
      if (ttTotal) ttTotal.textContent = `${item.total} solicitudes`;
      if (ttBadge) ttBadge.textContent = item.growth;
      if (ttResolved) ttResolved.textContent = String(item.purple);
      if (ttActive) ttActive.textContent = String(item.cyan);

      const plotW = plot.clientWidth;
      const plotH = plot.clientHeight;
      const xPct = ptC.x / VIEWBOX.width;
      const highestY = Math.min(ptC.y, ptP.y);
      const yPct = highestY / VIEWBOX.height;

      tooltip.style.left = `${xPct * plotW}px`;
      tooltip.style.top = `${Math.max(10, yPct * plotH - 12)}px`;
      tooltip.classList.add("is-visible");
    };

    svg.addEventListener("pointermove", (e) => {
      const svgRect = svg.getBoundingClientRect();
      const scaleX = VIEWBOX.width / svgRect.width;
      const pointerX = (e.clientX - svgRect.left) * scaleX;
      let nearest = Math.round((pointerX - bounds.left) / stepX);
      nearest = Math.max(0, Math.min(nearest, this.monthlyData.length - 1));
      showTooltip(nearest);
    });

    svg.addEventListener("pointerleave", () => {
      showTooltip(this.activeIndex);
    });

    showTooltip(activeIdx);
  }
}

customElements.define("cd-user-dashboard", UserDashboard);
