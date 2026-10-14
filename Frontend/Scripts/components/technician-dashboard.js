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

class TechnicianDashboard extends HTMLElement {
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
      console.error("Error al cargar dashboard del técnico:", err);
      this.loading = false;
      this.renderError();
    }
  }

  processData() {
    const assigned = Number(this.stats?.assignedTickets ?? 0);
    const inProgress = Number(this.stats?.inProgressTickets ?? 0);
    const resolved = Number(this.stats?.resolvedTickets ?? 0);
    const closed = Number(this.stats?.closedTickets ?? 0);

    const activeTotal = assigned + inProgress;
    const completedTotal = resolved + closed;
    const total = activeTotal + completedTotal;
    const buckets = Array.from({ length: 12 }, (_, idx) => ({
      index: idx,
      shortLabel: MONTH_LABELS[idx],
      fullLabel: MONTH_NAMES[idx],
      cyan: 0, 
      purple: 0,
      total: 0,
      growth: "0.0%",
      efficiency: "0%",
    }));
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
              } else if (ticket.status === "ASSIGNED" || ticket.status === "IN_PROGRESS") {
                buckets[m].cyan += 1;
              }
            }
          }
        }
      });
    }
    const currentMonth = new Date().getMonth();
    if (!hasRealTicketDates && total > 0) {
      buckets[currentMonth].cyan = activeTotal;
      buckets[currentMonth].purple = completedTotal;
    }
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
      <div class="loading-wrap">Cargando panel de soporte técnico…</div>
    `;
  }

  renderError() {
    this.shadowRoot.innerHTML = `
      <style>
        :host { display: block; color: #f87171; font-family: var(--cd-font-body, sans-serif); }
        .error-wrap { padding: 2rem; background: rgba(239, 68, 68, 0.1); border-radius: 12px; border: 1px solid rgba(239,68,68,0.2); }
      </style>
      <div class="error-wrap">No se pudieron sincronizar las métricas con el servidor.</div>
    `;
  }

  render() {
    const assigned = Number(this.stats?.assignedTickets ?? 0);
    const inProgress = Number(this.stats?.inProgressTickets ?? 0);
    const resolved = Number(this.stats?.resolvedTickets ?? 0);
    const closed = Number(this.stats?.closedTickets ?? 0);

    const total = assigned + inProgress + resolved + closed;
    const completed = resolved + closed;
    const efficiency = total > 0 ? Math.round((completed / total) * 100) : 0;
    const pctAssigned = total > 0 ? Math.min(100, Math.round((assigned / total) * 100)) : 0;
    const pctInProgress = total > 0 ? Math.min(100, Math.round((inProgress / total) * 100)) : 0;
    const pctResolved = total > 0 ? Math.min(100, Math.round((resolved / total) * 100)) : 0;
    const pctClosed = total > 0 ? Math.min(100, Math.round((closed / total) * 100)) : 0;
    const priorityTickets = this.tickets
      .filter((t) => t.status === "ASSIGNED" || t.status === "IN_PROGRESS" || t.status === "OPEN")
      .sort((a, b) => {
        const pOrder = { CRITICAL: 1, HIGH: 2, MEDIUM: 3, LOW: 4 };
        return (pOrder[a.priority] || 5) - (pOrder[b.priority] || 5);
      })
      .slice(0, 5);

    const queueItemsHtml = priorityTickets.length > 0
      ? priorityTickets.map((t) => {
          let iconClass = "priority-queue-item__icon--medium";
          let iconSymbol = "ℹ";
          if (t.priority === "CRITICAL") {
            iconClass = "priority-queue-item__icon--critical";
            iconSymbol = "⚠";
          } else if (t.priority === "HIGH") {
            iconClass = "priority-queue-item__icon--high";
            iconSymbol = "";
          }

          const statusLabel = t.status === "IN_PROGRESS" ? "EN ATENCIÓN" : "ASIGNADO";
          const statusClass = t.status === "IN_PROGRESS" ? "priority-queue-item__tag--status" : "";

          return `
            <a href="#assigned-tickets" class="priority-queue-item" data-ticket-id="${t.id}" title="Gestionar incidencia #${t.id}">
              <div class="priority-queue-item__icon ${iconClass}">
                ${iconSymbol}
              </div>
              <div class="priority-queue-item__content">
                <p class="priority-queue-item__title">#${t.id} — ${t.title || 'Sin asunto'}</p>
                <div class="priority-queue-item__meta">
                  <span class="priority-queue-item__tag">${t.category || 'GENERAL'}</span>
                  <span class="priority-queue-item__tag ${statusClass}">${statusLabel}</span>
                  <span style="color:#94a3b8;">Solicitante: <strong>${t.applicantUsername || 'Usuario'}</strong></span>
                </div>
              </div>
            </a>
          `;
        }).join("")
      : `<div class="empty-queue-msg">✓ No tienes tiquetes pendientes de atención en tu cola. ¡Todo al día!</div>`;
    this.shadowRoot.innerHTML = `
      <style>
        @import url("./stylesheet/tokens.css");
        @import url("./stylesheet/components/role-dashboard.css");
        @import url("./stylesheet/components/revenue-chart.css");
        :host { display: block; width: 100%; }
      </style>

      <div class="role-dashboard">
        <!-- Cabecera del Técnico -->
        <header class="role-dashboard__header">
          <div>
            <h1 class="role-dashboard__title">Panel de Control Operativo</h1>
            <p class="role-dashboard__subtitle">Supervisión en vivo de casos asignados, avance técnico y desempeño.</p>
          </div>
          <div>
            <span class="role-dashboard__badge-role">
              <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:#22d3ee; box-shadow:0 0 8px #22d3ee;"></span>
              Especialista de Soporte
            </span>
          </div>
        </header>

        <!-- Fila de 5 Tarjetas KPI con barras de progreso (Fortify360 Style) -->
        <section class="role-kpi-grid" aria-label="Indicadores del técnico">
          <!-- Card 1: Asignados -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--cyan">Por Iniciar</span>
            </div>
            <p class="role-kpi-card__label">Tiquetes Asignados</p>
            <strong class="role-kpi-card__value">${assigned}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--cyan" style="width: ${pctAssigned}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctAssigned}%</span>
            </div>
          </article>

          <!-- Card 2: En Proceso -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--amber">En Atención</span>
            </div>
            <p class="role-kpi-card__label">En Proceso</p>
            <strong class="role-kpi-card__value">${inProgress}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--amber" style="width: ${pctInProgress}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctInProgress}%</span>
            </div>
          </article>

          <!-- Card 3: Resueltos -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--green">Solucionados</span>
            </div>
            <p class="role-kpi-card__label">Resueltos por Mí</p>
            <strong class="role-kpi-card__value">${resolved}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--green" style="width: ${pctResolved}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctResolved}%</span>
            </div>
          </article>

          <!-- Card 4: Cerrados -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--purple">Finalizados</span>
            </div>
            <p class="role-kpi-card__label">Cerrados Definitivos</p>
            <strong class="role-kpi-card__value">${closed}</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--purple" style="width: ${pctClosed}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${pctClosed}%</span>
            </div>
          </article>

          <!-- Card 5: Tasa de Efectividad -->
          <article class="role-kpi-card">
            <div class="role-kpi-card__top">
              <span class="role-kpi-card__badge role-kpi-card__badge--green">Efectividad</span>
            </div>
            <p class="role-kpi-card__label">Tasa de Resolución</p>
            <strong class="role-kpi-card__value">${efficiency}%</strong>
            <div class="role-kpi-card__progress-wrap">
              <div class="role-kpi-card__progress-bar">
                <div class="role-kpi-card__progress-fill role-kpi-card__progress-fill--green" style="width: ${efficiency}%;"></div>
              </div>
              <span class="role-kpi-card__percent">${efficiency}%</span>
            </div>
          </article>
        </section>

        <!-- Grid de 2 Columnas: Gráfico Luminiscente + Cola de Prioridad -->
        <section class="role-dashboard-grid">
          <!-- Columna Izquierda: Gráfico de Ondas de Rendimiento -->
          <div class="revenue-chart">
            <div class="revenue-chart__header">
              <div class="revenue-chart__heading">
                <p class="revenue-chart__eyebrow">Rendimiento Técnico</p>
                <div class="revenue-chart__headline">
                  <span class="revenue-chart__main-val">${completed}</span>
                  <span class="revenue-chart__trend-badge">+${efficiency}% Eficiencia</span>
                </div>
              </div>
              <div class="revenue-chart__header-right">
                <div class="revenue-chart__legend">
                  <div class="revenue-chart__legend-item">
                    <span class="revenue-chart__legend-dot revenue-chart__legend-dot--purple"></span>
                    <span class="revenue-chart__legend-label">Resueltos</span>
                  </div>
                  <div class="revenue-chart__legend-item">
                    <span class="revenue-chart__legend-dot revenue-chart__legend-dot--cyan"></span>
                    <span class="revenue-chart__legend-label">En Atención</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="revenue-chart__plot" id="tech-plot">
              <svg class="revenue-chart__svg" id="tech-svg" viewBox="0 0 680 320" role="img" aria-label="Curva de rendimiento del técnico"></svg>
              
              <div class="revenue-chart__tooltip" id="tech-tooltip" role="status" aria-live="polite">
                <p class="revenue-chart__tooltip-date" id="tt-date">Octubre 2026</p>
                <div class="revenue-chart__tooltip-top">
                  <strong class="revenue-chart__tooltip-total" id="tt-total">0 tiquetes</strong>
                  <span class="revenue-chart__tooltip-growth" id="tt-badge">0.0%</span>
                </div>
                <div class="revenue-chart__tooltip-divider"></div>
                <div class="revenue-chart__tooltip-row">
                  <span class="tooltip-dot tooltip-dot--purple"></span>
                  <span class="tooltip-name">Resueltos:</span>
                  <strong id="tt-resolved">0</strong>
                </div>
                <div class="revenue-chart__tooltip-row">
                  <span class="tooltip-dot tooltip-dot--cyan"></span>
                  <span class="tooltip-name">En atención:</span>
                  <strong id="tt-active">0</strong>
                </div>
              </div>
            </div>
            
            <div class="revenue-chart__footer">
              <span class="revenue-chart__source">Métricas sincronizadas con Spring Boot</span>
            </div>
          </div>

          <!-- Columna Derecha: Cola de Prioridades de Tiquetes (Remediation Queue) -->
          <div class="priority-queue-card">
            <div class="priority-queue-card__header">
              <div>
                <h2 class="priority-queue-card__title">Cola de Casos Prioritarios</h2>
                <p class="priority-queue-card__subtitle">Casos asignados que requieren atención técnica</p>
              </div>
              <span class="priority-queue-card__badge-count">${priorityTickets.length} pendientes</span>
            </div>

            <div class="priority-queue-list">
              ${queueItemsHtml}
            </div>

            <a href="#assigned-tickets" class="quick-action-card__btn quick-action-card__btn--outline" style="margin-top:auto;">
              <span>Ver Bandeja Completa de Tiquetes</span>
              <span>→</span>
            </a>
          </div>
        </section>
      </div>
    `;
  }

  initChart() {
    const svg = this.shadowRoot.getElementById("tech-svg");
    const plot = this.shadowRoot.getElementById("tech-plot");
    const tooltip = this.shadowRoot.getElementById("tech-tooltip");

    if (!svg || !plot || !tooltip) return;

    svg.replaceChildren();

    // Defs y gradientes
    const defs = createSvg("defs");
    const cyanGrad = createSvg("linearGradient", { id: "techCyanArea", x1: "0", y1: "0", x2: "0", y2: "1" });
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

    // Área luminosa Cyan
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-area",
        d: getCurvedAreaPath(this.pointsCyan, bounds.bottom),
        fill: "url(#techCyanArea)",
      })
    );

    // Curva Púrpura
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--purple",
        d: getCurvedPath(this.pointsPurple),
      })
    );

    // Curva Cyan
    svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--cyan",
        d: getCurvedPath(this.pointsCyan),
      })
    );

    // Puntos destacados en mes 9 (octubre)
    const activeIdx = Math.min(this.activeIndex, this.pointsCyan.length - 1);
    const pC = this.pointsCyan[activeIdx];
    const pP = this.pointsPurple[activeIdx];

    svg.append(
      createSvg("circle", { cx: pC.x, cy: pC.y, r: 4, fill: "#22d3ee", stroke: "#070f24", "stroke-width": 2 }),
      createSvg("circle", { cx: pP.x, cy: pP.y, r: 4, fill: "#c084fc", stroke: "#070f24", "stroke-width": 2 })
    );

    // Etiquetas X
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

    // Eventos de puntero para el tooltip
    const showTooltip = (idx) => {
      const item = this.monthlyData[idx];
      const ptC = this.pointsCyan[idx];
      const ptP = this.pointsPurple[idx];
      if (!item || !ptC) return;

      const ttDate = this.shadowRoot.getElementById("tt-date");
      const ttTotal = this.shadowRoot.getElementById("tt-total");
      const ttBadge = this.shadowRoot.getElementById("tt-badge");
      const ttResolved = this.shadowRoot.getElementById("tt-resolved");
      const ttActive = this.shadowRoot.getElementById("tt-active");

      if (ttDate) ttDate.textContent = `${item.fullLabel} ${new Date().getFullYear()}`;
      if (ttTotal) ttTotal.textContent = `${item.total} tiquetes`;
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

customElements.define("cd-technician-dashboard", TechnicianDashboard);
