/**
 * Componente de Gráfico de Líneas de Rendimiento y Productividad de Tiquetes.
 * Implementa curvas cúbicas Bezier con relleno degradado luminiscente
 * basado en los datos reales de la API Spring Boot (GET /api/dashboard/stats y GET /api/tickets).
 * Desarrollado 100% en Vanilla JS, HTML y SVG sin dependencias externas.
 */
import { apiRequest } from "../config/http-client.js";

const SVG_NS = "http://www.w3.org/2000/svg";
const VIEWBOX = { width: 1000, height: 440, top: 40, right: 40, bottom: 380, left: 60 };

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

export class RevenueChart {
  constructor(target, options = {}) {
    this.target = typeof target === "string" ? document.querySelector(target) : target;
    this.options = {
      templateSelector: "#revenue-chart-template",
      ...options,
    };
    this.stats = null;
    this.tickets = [];
    this.monthlyData = [];
    this.pointsCyan = [];
    this.pointsPurple = [];
    this.activeIndex = 9; // Octubre por defecto (mes actual)
    this.abortController = null;
  }

  async mount() {
    if (!(this.target instanceof HTMLElement)) {
      throw new Error("RevenueChart requires a valid host element.");
    }

    const template = document.querySelector(this.options.templateSelector);
    if (!(template instanceof HTMLTemplateElement)) {
      throw new Error("RevenueChart could not find its HTML template.");
    }

    this.target.replaceChildren(template.content.cloneNode(true));
    this.root = this.target.querySelector(".revenue-chart");
    this.svg = this.root.querySelector("[data-chart-svg]");
    this.plot = this.root.querySelector("[data-chart-plot]");
    this.tooltip = this.root.querySelector("[data-chart-tooltip]");
    this.statusEl = this.root.querySelector("[data-chart-status]");

    // Elementos de métricas del DOM
    this.ui = {
      total: this.root.querySelector("[data-stat-total]"),
      growth: this.root.querySelector("[data-stat-growth]"),
      open: this.root.querySelector("[data-summary-open]"),
      progress: this.root.querySelector("[data-summary-progress]"),
      done: this.root.querySelector("[data-summary-done]"),
      efficiency: this.root.querySelector("[data-summary-efficiency]"),
      tooltipDate: this.root.querySelector("[data-tooltip-date]"),
      tooltipTotal: this.root.querySelector("[data-tooltip-total]"),
      tooltipBadge: this.root.querySelector("[data-tooltip-badge]"),
      tooltipResolved: this.root.querySelector("[data-tooltip-resolved]"),
      tooltipOpen: this.root.querySelector("[data-tooltip-open]"),
      tooltipEfficiency: this.root.querySelector("[data-tooltip-efficiency]"),
    };

    this.bindEvents();
    await this.loadData();
    return this;
  }

  bindEvents() {
    this.svg.addEventListener("pointermove", (e) => this.handlePointerMove(e));
    this.svg.addEventListener("pointerleave", () => this.handlePointerLeave());
    window.addEventListener("resize", () => {
      if (this.monthlyData.length) {
        this.renderChart();
      }
    });
  }

  async loadData() {
    this.setStatus("Consultando métricas en vivo con Spring Boot…");

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
      this.updateKPIs();
      this.renderChart();
      this.setStatus("Métricas en vivo sincronizadas con PostgreSQL");
    } catch (err) {
      console.error("Error al cargar datos del dashboard:", err);
      this.setStatus("No se pudieron cargar las estadísticas.");
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

    // Inicializar los 12 meses exclusivamente en cero (sin datos simulados)
    const buckets = Array.from({ length: 12 }, (_, idx) => ({
      index: idx,
      shortLabel: MONTH_LABELS[idx],
      fullLabel: MONTH_NAMES[idx],
      cyan: 0,   // Abiertos / En proceso
      purple: 0, // Resueltos / Cerrados
      total: 0,
      growth: "0.0%",
      efficiency: "0%",
    }));

    // Agrupar los tickets reales obtenidos desde PostgreSQL por su mes de creación
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

    // Si no hay fechas individuales pero el backend reporta tickets en stats,
    // reflejarlos en el mes actual del sistema
    const currentMonth = new Date().getMonth();
    if (!hasRealTicketDates && total > 0) {
      buckets[currentMonth].cyan = activeTotal;
      buckets[currentMonth].purple = completedTotal;
    }

    // Calcular métricas reales mes a mes
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

  updateKPIs() {
    const total = Number(this.stats?.totalTickets ?? this.tickets.length);
    const open = Number(this.stats?.openTickets ?? 0);
    const assigned = Number(this.stats?.assignedTickets ?? 0);
    const inProgress = Number(this.stats?.inProgressTickets ?? 0);
    const resolved = Number(this.stats?.resolvedTickets ?? 0);
    const closed = Number(this.stats?.closedTickets ?? 0);

    const active = open + assigned + inProgress;
    const done = resolved + closed;
    const efficiencyRate = total > 0 ? ((done / total) * 100).toFixed(1) : "0.0";

    if (this.ui.total) this.ui.total.textContent = total.toLocaleString("es-CO");
    if (this.ui.growth) this.ui.growth.textContent = total > 0 ? `${efficiencyRate}% Eficiencia` : "0.0% —";
    if (this.ui.open) this.ui.open.textContent = open.toLocaleString("es-CO");
    if (this.ui.progress) this.ui.progress.textContent = (assigned + inProgress).toLocaleString("es-CO");
    if (this.ui.done) this.ui.done.textContent = done.toLocaleString("es-CO");
    if (this.ui.efficiency) this.ui.efficiency.textContent = `${efficiencyRate}%`;
  }

  renderChart() {
    this.svg.replaceChildren();

    // 1. Definición de gradientes y filtros
    const defs = createSvg("defs");

    // Gradiente de relleno Cyan (área bajo la curva azul)
    const cyanGrad = createSvg("linearGradient", { id: "cyanWaveArea", x1: "0", y1: "0", x2: "0", y2: "1" });
    cyanGrad.append(
      createSvg("stop", { offset: "0%", "stop-color": "#22d3ee", "stop-opacity": "0.38" }),
      createSvg("stop", { offset: "45%", "stop-color": "#0284c7", "stop-opacity": "0.15" }),
      createSvg("stop", { offset: "100%", "stop-color": "#070f24", "stop-opacity": "0.0" }),
    );

    // Gradiente de relleno Purple
    const purpleGrad = createSvg("linearGradient", { id: "purpleWaveArea", x1: "0", y1: "0", x2: "0", y2: "1" });
    purpleGrad.append(
      createSvg("stop", { offset: "0%", "stop-color": "#c084fc", "stop-opacity": "0.22" }),
      createSvg("stop", { offset: "60%", "stop-color": "#9333ea", "stop-opacity": "0.08" }),
      createSvg("stop", { offset: "100%", "stop-color": "#070f24", "stop-opacity": "0.0" }),
    );

    defs.append(cyanGrad, purpleGrad);
    this.svg.append(defs);

    // 2. Cálculo de métricas y coordenadas
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
    // Techo redondeado agradable (múltiplo de 5 o 10)
    const ceiling = Math.max(4, Math.ceil((maxVal * 1.2) / 2) * 2);

    const toY = (val) => bounds.bottom - (val / ceiling) * (bounds.bottom - bounds.top);
    const stepX = (bounds.right - bounds.left) / (this.monthlyData.length - 1);
    const toX = (idx) => bounds.left + idx * stepX;

    // 3. Renderizar Rejilla y Eje Y
    const gridGroup = createSvg("g", { class: "revenue-chart__grid" });
    const steps = 4;
    for (let i = 0; i <= steps; i++) {
      const val = Math.round((ceiling / steps) * i);
      const y = toY(val);

      // Línea horizontal punteada
      gridGroup.append(
        createSvg("line", {
          class: "revenue-chart__grid-line",
          x1: bounds.left,
          y1: y,
          x2: bounds.right,
          y2: y,
        })
      );

      // Texto eje Y
      const text = createSvg("text", {
        class: "revenue-chart__axis-text revenue-chart__axis-text--y",
        x: bounds.left - 12,
        y: y + 4,
      });
      text.textContent = val >= 1000 ? `${(val / 1000).toFixed(0)}k` : String(val);
      gridGroup.append(text);
    }
    this.svg.append(gridGroup);

    // 4. Mapear puntos de ambas series
    this.pointsCyan = this.monthlyData.map((d, i) => ({ x: toX(i), y: toY(d.cyan), data: d }));
    this.pointsPurple = this.monthlyData.map((d, i) => ({ x: toX(i), y: toY(d.purple), data: d }));

    // 5. Renderizar área luminiscente bajo la curva Cyan (como en la imagen)
    const cyanAreaPath = getCurvedAreaPath(this.pointsCyan, bounds.bottom);
    this.svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-area",
        d: cyanAreaPath,
        fill: "url(#cyanWaveArea)",
      })
    );

    // 6. Renderizar curvas de línea
    const purpleLinePath = getCurvedPath(this.pointsPurple);
    this.svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--purple",
        d: purpleLinePath,
      })
    );

    const cyanLinePath = getCurvedPath(this.pointsCyan);
    this.svg.append(
      createSvg("path", {
        class: "revenue-chart__wave-line revenue-chart__wave-line--cyan",
        d: cyanLinePath,
      })
    );

    // 7. Renderizar puntos destacados en el extremo final (mes 11) o mes actual
    const highlightIdx = Math.min(this.activeIndex, this.pointsCyan.length - 1);
    const pC = this.pointsCyan[highlightIdx];
    const pP = this.pointsPurple[highlightIdx];

    // Puntos brillantes
    this.svg.append(
      createSvg("circle", { cx: pC.x, cy: pC.y, r: 4.5, fill: "#22d3ee", stroke: "#070f24", "stroke-width": 2 }),
      createSvg("circle", { cx: pP.x, cy: pP.y, r: 4.5, fill: "#c084fc", stroke: "#070f24", "stroke-width": 2 })
    );

    // 8. Elementos interactivos (guía vertical y puntos de hover)
    this.guideLine = createSvg("line", {
      class: "revenue-chart__guide-line",
      x1: pC.x,
      y1: bounds.top,
      x2: pC.x,
      y2: bounds.bottom,
      opacity: "0",
    });

    this.activeDotCyan = createSvg("circle", {
      class: "revenue-chart__active-dot",
      cx: pC.x,
      cy: pC.y,
      r: 6,
      fill: "#22d3ee",
      stroke: "#070f24",
      "stroke-width": 2.5,
      opacity: "0",
    });

    this.activeDotPurple = createSvg("circle", {
      class: "revenue-chart__active-dot",
      cx: pP.x,
      cy: pP.y,
      r: 6,
      fill: "#c084fc",
      stroke: "#070f24",
      "stroke-width": 2.5,
      opacity: "0",
    });

    this.svg.append(this.guideLine, this.activeDotCyan, this.activeDotPurple);

    // 9. Renderizar etiquetas del Eje X (meses)
    this.xLabels = [];
    const xLabelsGroup = createSvg("g", { class: "revenue-chart__x-labels" });
    this.monthlyData.forEach((d, i) => {
      const text = createSvg("text", {
        class: `revenue-chart__axis-text revenue-chart__axis-text--x ${i === highlightIdx ? "is-active" : ""}`,
        x: toX(i),
        y: bounds.bottom + 26,
      });
      text.textContent = d.shortLabel;
      xLabelsGroup.append(text);
      this.xLabels.push(text);
    });
    this.svg.append(xLabelsGroup);

    // Posicionar tooltip en el mes activo inicial
    this.showTooltipAt(highlightIdx);
  }

  handlePointerMove(event) {
    if (!this.pointsCyan.length) return;

    const svgRect = this.svg.getBoundingClientRect();
    const scaleX = VIEWBOX.width / svgRect.width;
    const pointerX = (event.clientX - svgRect.left) * scaleX;

    const bounds = {
      left: VIEWBOX.left,
      right: VIEWBOX.width - VIEWBOX.right,
    };
    const stepX = (bounds.right - bounds.left) / (this.monthlyData.length - 1);

    // Encontrar el mes más cercano al cursor
    let nearestIdx = Math.round((pointerX - bounds.left) / stepX);
    nearestIdx = Math.max(0, Math.min(nearestIdx, this.monthlyData.length - 1));

    this.showTooltipAt(nearestIdx);
  }

  handlePointerLeave() {
    // Al salir, dejar posicionado en el mes actual (octubre)
    this.showTooltipAt(new Date().getMonth());
  }

  showTooltipAt(index) {
    if (!this.pointsCyan[index]) return;

    this.activeIndex = index;
    const item = this.monthlyData[index];
    const pC = this.pointsCyan[index];
    const pP = this.pointsPurple[index];

    // Actualizar guía vertical
    if (this.guideLine) {
      this.guideLine.setAttribute("x1", String(pC.x));
      this.guideLine.setAttribute("x2", String(pC.x));
      this.guideLine.setAttribute("opacity", "1");
    }

    // Actualizar puntos de hover
    if (this.activeDotCyan) {
      this.activeDotCyan.setAttribute("cx", String(pC.x));
      this.activeDotCyan.setAttribute("cy", String(pC.y));
      this.activeDotCyan.setAttribute("opacity", "1");
    }

    if (this.activeDotPurple) {
      this.activeDotPurple.setAttribute("cx", String(pP.x));
      this.activeDotPurple.setAttribute("cy", String(pP.y));
      this.activeDotPurple.setAttribute("opacity", "1");
    }

    // Resaltar etiqueta X activa
    if (this.xLabels) {
      this.xLabels.forEach((el, idx) => {
        el.classList.toggle("is-active", idx === index);
      });
    }

    // Contenido del tooltip
    if (this.ui.tooltipDate) this.ui.tooltipDate.textContent = `${item.fullLabel} ${new Date().getFullYear()}`;
    if (this.ui.tooltipTotal) this.ui.tooltipTotal.textContent = `${item.total} incidencias`;
    if (this.ui.tooltipBadge) this.ui.tooltipBadge.textContent = item.growth;
    if (this.ui.tooltipResolved) this.ui.tooltipResolved.textContent = String(item.purple);
    if (this.ui.tooltipOpen) this.ui.tooltipOpen.textContent = String(item.cyan);
    if (this.ui.tooltipEfficiency) this.ui.tooltipEfficiency.textContent = item.efficiency;

    // Posición del tooltip en coordenadas de pantalla relativas a plot
    const plotWidth = this.plot.clientWidth;
    const plotHeight = this.plot.clientHeight;
    const xPct = pC.x / VIEWBOX.width;
    const highestY = Math.min(pC.y, pP.y);
    const yPct = highestY / VIEWBOX.height;

    const xPx = xPct * plotWidth;
    const yPx = Math.max(10, yPct * plotHeight - 14);

    this.tooltip.style.left = `${xPx}px`;
    this.tooltip.style.top = `${yPx}px`;
    this.tooltip.classList.add("is-visible");
    this.tooltip.setAttribute("aria-hidden", "false");
  }

  setStatus(msg) {
    if (this.statusEl) {
      this.statusEl.textContent = msg;
    }
  }

  destroy() {
    this.abortController?.abort();
    this.target?.replaceChildren();
  }
}
