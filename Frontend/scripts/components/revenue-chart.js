const SVG_NAMESPACE = "http://www.w3.org/2000/svg";
const DEFAULT_ENDPOINT = "http://localhost:8051/api/dashboard/stats";
const CHART_VIEWBOX = { width: 960, height: 420, top: 26, right: 26, bottom: 60, left: 70 };

const STATUS_BARS = [
  { key: "openTickets", label: "Abiertos", colorVar: "--cd-color-data-revenue" },
  { key: "assignedTickets", label: "Asignados", colorVar: "--cd-color-data-expenses" },
  { key: "inProgressTickets", label: "En proceso", colorVar: "--cd-color-data-resolved" },
  { key: "resolvedTickets", label: "Resueltos", colorVar: "--cd-color-data-closed" },
  { key: "closedTickets", label: "Cerrados", colorVar: "--cd-color-data-done" },
];

const COMPARISON_BARS = [
  { key: "opened", label: "Tiquetes abiertos", colorVar: "--cd-color-data-revenue" },
  { key: "done", label: "Tiquetes completados", colorVar: "--cd-color-data-expenses" },
];

function createSvgElement(name, attributes = {}) {
  const element = document.createElementNS(SVG_NAMESPACE, name);
  Object.entries(attributes).forEach(([attribute, value]) => element.setAttribute(attribute, String(value)));
  return element;
}

function toFiniteNumber(value) {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }
  if (typeof value === "string") {
    const normalized = value.replace(/[^0-9.-]/g, "");
    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : null;
  }
  return null;
}

export class DashboardStatsService {
  constructor({ endpoint = DEFAULT_ENDPOINT } = {}) {
    this.endpoint = endpoint;
  }

  async fetchStats(signal) {
    const authToken = sessionStorage.getItem("authToken");
    const headers = { Accept: "application/json" };
    if (authToken) {
      headers.Authorization = `Bearer ${authToken}`;
    }

    const response = await fetch(this.endpoint, { headers, signal });
    if (!response.ok) {
      throw new Error(`Dashboard request failed with status ${response.status}.`);
    }
    return response.json();
  }
}

export class RevenueChart {
  constructor(target, options = {}) {
    this.target = typeof target === "string" ? document.querySelector(target) : target;
    this.options = {
      endpoint: DEFAULT_ENDPOINT,
      templateSelector: "#revenue-chart-template",
      ...options,
    };
    this.statsService = new DashboardStatsService({ endpoint: this.options.endpoint });
    this.statusData = [];
    this.comparisonData = [];
    this.geometry = null;
    this.activeBarIndex = null;
    this.abortController = null;
    this.eventController = new AbortController();
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
    this.status = this.root.querySelector("[data-chart-status]");
    this.summary = {
      opened: this.root.querySelector("[data-summary-opened]"),
      done: this.root.querySelector("[data-summary-done]"),
    };
    this.tooltipFields = {
      label: this.root.querySelector("[data-tooltip-label]"),
      opened: this.root.querySelector("[data-tooltip-opened]"),
      done: this.root.querySelector("[data-tooltip-done]"),
      efficiency: this.root.querySelector("[data-tooltip-efficiency]"),
    };

    this.bindEvents();
    await this.loadData();
    return this;
  }

  bindEvents() {
    const { signal } = this.eventController;

    this.svg.addEventListener("pointermove", (event) => this.handlePointerMove(event), { signal });
    this.svg.addEventListener("pointerleave", () => this.hideTooltip(), { signal });
    this.svg.addEventListener("click", (event) => this.handlePointerMove(event), { signal });
    this.svg.addEventListener("keydown", (event) => this.handleKeyboardNavigation(event), { signal });
  }

  async loadData() {
    this.abortController?.abort();
    this.abortController = new AbortController();
    this.setStatus("Cargando datos en vivo…");

    try {
      const payload = await this.statsService.fetchStats(this.abortController.signal);
      this.mapPayload(payload);
      this.setStatus("Métricas de tiquetes cargadas en vivo");
    } catch (error) {
      if (error.name === "AbortError") {
        return;
      }
      this.setStatus("No se pudieron cargar los datos en vivo.");
      console.error("Dashboard data load failed:", error);
    }

    this.activeBarIndex = null;
    this.renderChart();
  }

  mapPayload(payload) {
    const opened = toFiniteNumber(payload?.totalTickets) ?? 0;
    const resolved = toFiniteNumber(payload?.resolvedTickets) ?? 0;
    const closed = toFiniteNumber(payload?.closedTickets) ?? 0;
    const done = resolved + closed;

    this.statusData = STATUS_BARS.map((bar) => ({
      ...bar,
      value: toFiniteNumber(payload?.[bar.key]) ?? 0,
    }));

    this.comparisonData = COMPARISON_BARS.map((bar) => ({
      ...bar,
      value: bar.key === "opened" ? opened : done,
    }));
  }

  renderChart() {
    this.geometry = this.createChartMetrics();
    this.svg.replaceChildren();
    this.renderGrid(this.geometry);
    this.renderBars(this.geometry);
    this.renderBarLabels(this.geometry);
    this.updateSummary();
  }

  createChartMetrics() {
    const bounds = {
      left: CHART_VIEWBOX.left,
      right: CHART_VIEWBOX.width - CHART_VIEWBOX.right,
      top: CHART_VIEWBOX.top,
      bottom: CHART_VIEWBOX.height - CHART_VIEWBOX.bottom,
    };

    const allValues = [
      ...this.statusData.map((d) => d.value),
      ...this.comparisonData.map((d) => d.value),
    ];
    const maxValue = Math.max(...allValues, 10);
    const ceiling = Math.ceil((maxValue * 1.15) / 10) * 10;
    const scaleY = (bounds.bottom - bounds.top) / ceiling;
    const toY = (value) => bounds.bottom - value * scaleY;

    const groupCount = 2;
    const groupGap = 80;
    const groupWidth = (bounds.right - bounds.left - groupGap * (groupCount - 1)) / groupCount;

    const buildGroup = (data, groupIndex) => {
      const groupStart = bounds.left + (groupWidth + groupGap) * groupIndex;
      const barGap = 12;
      const barWidth = Math.min(64, (groupWidth - barGap * (data.length - 1)) / data.length);

      return data.map((item, index) => ({
        ...item,
        x: groupStart + (barWidth + barGap) * index,
        barWidth,
        y: toY(item.value),
        groupIndex,
      }));
    };

    const statusBars = buildGroup(this.statusData, 0);
    const comparisonBars = buildGroup(this.comparisonData, 1);

    return {
      bounds,
      ceiling,
      toY,
      scaleY,
      statusBars,
      comparisonBars,
    };
  }

  renderGrid({ bounds, ceiling, toY }) {
    const gridGroup = createSvgElement("g", { class: "revenue-chart__grid" });
    const axisSteps = 4;

    for (let step = 0; step <= axisSteps; step += 1) {
      const value = Math.round((ceiling / axisSteps) * step);
      const y = toY(value);
      gridGroup.append(
        createSvgElement("line", { class: "revenue-chart__grid-line", x1: bounds.left, y1: y, x2: bounds.right, y2: y }),
        this.createTextLabel(value.toLocaleString("en-US"), bounds.left - 12, y + 3.5, "revenue-chart__axis-label", "end"),
      );
    }

    this.svg.append(gridGroup);
  }

  renderBars({ statusBars, comparisonBars }) {
    const barsGroup = createSvgElement("g", { class: "revenue-chart__bars" });

    statusBars.forEach((bar, index) => {
      barsGroup.append(this.createBar(bar, index, "status"));
    });

    comparisonBars.forEach((bar, index) => {
      barsGroup.append(this.createBar(bar, index, "comparison"));
    });

    this.svg.append(barsGroup);
  }

  createBar(bar, index, groupType) {
    const { bounds } = this.geometry;
    const height = bounds.bottom - bar.y;
    const radius = Math.min(8, bar.barWidth / 4, height / 2);

    const rect = createSvgElement("rect", {
      class: `revenue-chart__bar revenue-chart__bar--${groupType}`,
      x: bar.x,
      y: bar.y,
      width: bar.barWidth,
      height: Math.max(0, height),
      rx: radius,
      fill: `var(${bar.colorVar})`,
      "data-bar-index": index,
      "data-bar-group": groupType,
      "data-bar-key": bar.key,
      "data-bar-value": bar.value,
      "data-bar-label": bar.label,
      "data-bar-color": `var(${bar.colorVar})`,
      tabindex: "0",
      role: "img",
      "aria-label": `${bar.label}: ${bar.value} tiquetes`,
    });

    const hitArea = createSvgElement("rect", {
      class: "revenue-chart__bar-hit",
      x: bar.x - 6,
      y: bounds.top,
      width: bar.barWidth + 12,
      height: bounds.bottom - bounds.top,
      fill: "transparent",
      "data-bar-index": index,
      "data-bar-group": groupType,
    });

    const group = createSvgElement("g", {
      class: `revenue-chart__bar-group revenue-chart__bar-group--${groupType}`,
    });
    group.append(rect, hitArea);

    return group;
  }

  renderBarLabels({ statusBars, comparisonBars, bounds }) {
    const labelsGroup = createSvgElement("g", { class: "revenue-chart__bar-labels" });

    statusBars.forEach((bar) => {
      labelsGroup.append(
        this.createTextLabel(bar.label, bar.x + bar.barWidth / 2, bounds.bottom + 20, "revenue-chart__bar-label", "middle"),
        this.createTextLabel(bar.value.toLocaleString("en-US"), bar.x + bar.barWidth / 2, bar.y - 8, "revenue-chart__bar-value", "middle"),
      );
    });

    comparisonBars.forEach((bar) => {
      labelsGroup.append(
        this.createTextLabel(bar.label, bar.x + bar.barWidth / 2, bounds.bottom + 20, "revenue-chart__bar-label", "middle"),
        this.createTextLabel(bar.value.toLocaleString("en-US"), bar.x + bar.barWidth / 2, bar.y - 8, "revenue-chart__bar-value", "middle"),
      );
    });

    this.svg.append(labelsGroup);
  }

  createTextLabel(content, x, y, className, anchor) {
    const label = createSvgElement("text", { class: className, x, y, "text-anchor": anchor });
    label.textContent = content;
    return label;
  }

  updateSummary() {
    const opened = this.comparisonData.find((d) => d.key === "opened");
    const done = this.comparisonData.find((d) => d.key === "done");

    this.summary.opened.textContent = opened ? `${opened.value.toLocaleString("en-US")} tickets` : "—";
    this.summary.done.textContent = done ? `${done.value.toLocaleString("en-US")} tickets` : "—";
  }

  handlePointerMove(event) {
    if (!this.geometry) return;

    const barGroups = this.svg.querySelectorAll(".revenue-chart__bar-group");
    if (!barGroups.length) return;

    const svgRect = this.svg.getBoundingClientRect();
    const scaleX = CHART_VIEWBOX.width / svgRect.width;
    const pointerX = (event.clientX - svgRect.left) * scaleX;

    let nearest = null;
    let nearestDistance = Infinity;

    barGroups.forEach((group) => {
      const bar = group.querySelector(".revenue-chart__bar");
      if (!bar) return;
      const x = Number(bar.getAttribute("x"));
      const width = Number(bar.getAttribute("width"));
      const center = x + width / 2;
      const distance = Math.abs(pointerX - center);
      if (distance < nearestDistance) {
        nearestDistance = distance;
        nearest = { group, bar };
      }
    });

    if (nearest) {
      const index = Number(nearest.bar.getAttribute("data-bar-index"));
      const groupType = nearest.bar.getAttribute("data-bar-group");
      this.showTooltip(index, groupType, nearest.bar);
    }
  }

  handleKeyboardNavigation(event) {
    if (!this.comparisonData.length && !this.statusData.length) return;

    const allBars = [...this.geometry.statusBars, ...this.comparisonData];
    let nextIndex = this.activeBarIndex ?? 0;

    if (event.key === "ArrowRight") {
      nextIndex = Math.min(nextIndex + 1, allBars.length - 1);
    } else if (event.key === "ArrowLeft") {
      nextIndex = Math.max(nextIndex - 1, 0);
    } else if (event.key === "Home") {
      nextIndex = 0;
    } else if (event.key === "End") {
      nextIndex = allBars.length - 1;
    } else if (event.key === "Escape") {
      this.hideTooltip();
      return;
    } else {
      return;
    }

    event.preventDefault();
    const target = allBars[nextIndex];
    const groupType = nextIndex < this.geometry.statusBars.length ? "status" : "comparison";
    const barIndex = groupType === "status" ? nextIndex : nextIndex - this.geometry.statusBars.length;

    const barEl = this.svg.querySelector(`.revenue-chart__bar[data-bar-group="${groupType}"][data-bar-index="${barIndex}"]`);
    if (barEl) {
      this.showTooltip(barIndex, groupType, barEl);
    }
  }

  showTooltip(index, groupType, barEl) {
    const label = barEl.getAttribute("data-bar-label") || "—";
    const value = Number(barEl.getAttribute("data-bar-value")) || 0;
    const opened = this.comparisonData.find((d) => d.key === "opened")?.value ?? 0;
    const done = this.comparisonData.find((d) => d.key === "done")?.value ?? 0;
    const efficiency = opened > 0 ? ((done / opened) * 100).toFixed(1) : null;

    this.activeBarIndex = groupType === "status" ? index : this.geometry.statusBars.length + index;

    this.svg.querySelectorAll(".revenue-chart__bar").forEach((node) => {
      node.classList.toggle("is-active", node === barEl);
    });

    this.tooltipFields.label.textContent = label;
    this.tooltipFields.opened.textContent = `${opened.toLocaleString("en-US")} tickets`;
    this.tooltipFields.done.textContent = `${done.toLocaleString("en-US")} tickets`;
    this.tooltipFields.efficiency.textContent = efficiency === null ? "Sin datos" : `${efficiency}% completados`;

    const plotWidth = this.plot.clientWidth;
    const plotHeight = this.plot.clientHeight;
    const barX = Number(barEl.getAttribute("x"));
    const barWidth = Number(barEl.getAttribute("width"));
    const xPercent = (barX + barWidth / 2) / CHART_VIEWBOX.width;
    const tooltipWidth = 220;
    const x = Math.min(plotWidth - tooltipWidth / 2, Math.max(tooltipWidth / 2, xPercent * plotWidth));
    const y = 80;

    this.tooltip.style.left = `${x}px`;
    this.tooltip.style.top = `${y}px`;
    this.tooltip.setAttribute("aria-hidden", "false");
    this.tooltip.classList.add("is-visible");
  }

  hideTooltip() {
    this.activeBarIndex = null;
    this.svg?.querySelectorAll(".revenue-chart__bar").forEach((node) => node.classList.remove("is-active"));
    this.tooltip?.classList.remove("is-visible");
    this.tooltip?.setAttribute("aria-hidden", "true");
  }

  setStatus(message) {
    this.status.textContent = message;
  }

  destroy() {
    this.abortController?.abort();
    this.eventController.abort();
    this.target?.replaceChildren();
  }
}
