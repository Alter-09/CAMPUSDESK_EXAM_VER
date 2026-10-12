const SVG_NAMESPACE = "http://www.w3.org/2000/svg";
const DEFAULT_ENDPOINT = "http://localhost:8051/api/dashboard/stats";
const CHART_VIEWBOX = { width: 960, height: 420, top: 26, right: 26, bottom: 52, left: 70 };
const SERIES_VISIBILITY = { created: true, completed: true };

const DEFAULT_MONTHLY_DATA = [
  ["Jan", 342, 218], ["Feb", 385, 245], ["Mar", 361, 232], ["Apr", 426, 273],
  ["May", 462, 281], ["Jun", 438, 294], ["Jul", 504, 318], ["Aug", 527, 305],
  ["Sep", 488, 329], ["Oct", 562, 347], ["Nov", 593, 362], ["Dec", 628, 381],
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
    const normalizedValue = value.replace(/[^0-9.-]/g, "");
    const parsedValue = Number(normalizedValue);
    return Number.isFinite(parsedValue) ? parsedValue : null;
  }

  return null;
}

function resolveProperty(record, names) {
  if (!record || typeof record !== "object") {
    return undefined;
  }

  const propertyName = names.find((name) => Object.prototype.hasOwnProperty.call(record, name));
  return propertyName ? record[propertyName] : undefined;
}

function createMonthDate(value, fallbackYear, fallbackIndex) {
  if (value instanceof Date && !Number.isNaN(value.valueOf())) {
    return value;
  }

  if (typeof value === "string" || typeof value === "number") {
    const parsedDate = new Date(value);
    if (!Number.isNaN(parsedDate.valueOf())) {
      return parsedDate;
    }
  }

  return new Date(fallbackYear, fallbackIndex, 1);
}

function createFallbackDataset(year) {
  return DEFAULT_MONTHLY_DATA.map(([label, created, completed], index) => ({
    label,
    date: new Date(year, index, 1),
    created,
    completed,
  }));
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
    this.seriesVisibility = { ...SERIES_VISIBILITY };
    this.data = [];
    this.geometry = null;
    this.activeIndex = null;
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
    this.rangeSelect = this.root.querySelector("[data-chart-range]");
    this.status = this.root.querySelector("[data-chart-status]");
    this.summary = {
      revenue: this.root.querySelector("[data-summary-revenue]"),
      expenses: this.root.querySelector("[data-summary-expenses]"),
    };
    this.tooltipFields = {
      date: this.root.querySelector("[data-tooltip-date]"),
      revenue: this.root.querySelector("[data-tooltip-revenue]"),
      expenses: this.root.querySelector("[data-tooltip-expenses]"),
      growth: this.root.querySelector("[data-tooltip-growth]"),
    };

    this.bindEvents();
    await this.loadData();
    return this;
  }

  bindEvents() {
    const { signal } = this.eventController;

    this.rangeSelect.addEventListener("change", () => this.loadData(), { signal });
    this.root.querySelectorAll("[data-series-toggle]").forEach((button) => {
      button.addEventListener("click", () => {
        const seriesName = button.dataset.seriesToggle;
        this.seriesVisibility[seriesName] = !this.seriesVisibility[seriesName];
        button.setAttribute("aria-pressed", String(this.seriesVisibility[seriesName]));
        this.renderChart();
      }, { signal });
    });

    this.svg.addEventListener("pointermove", (event) => this.handlePointerMove(event), { signal });
    this.svg.addEventListener("pointerleave", () => this.hideTooltip(), { signal });
    this.svg.addEventListener("click", (event) => this.handlePointerMove(event), { signal });
    this.svg.addEventListener("keydown", (event) => this.handleKeyboardNavigation(event), { signal });
  }

  async loadData() {
    this.abortController?.abort();
    this.abortController = new AbortController();
    this.setStatus("Loading live data…");

    try {
      const payload = await this.statsService.fetchStats(this.abortController.signal);
      const mappedData = this.mapPayloadToMonthlyData(payload);
      const selectedYear = Number(this.rangeSelect.value);

      if (mappedData.length) {
        this.data = mappedData;
        this.setStatus("Live ticket metrics loaded");
      } else {
        this.data = createFallbackDataset(selectedYear);
        this.setStatus("Live data has no series. Showing demo data.");
      }
    } catch (error) {
      if (error.name === "AbortError") {
        return;
      }

      this.data = createFallbackDataset(Number(this.rangeSelect.value));
      this.setStatus("Demo data shown because live data is unavailable.");
    }

    this.activeIndex = null;
    this.renderChart();
  }

  mapPayloadToMonthlyData(payload) {
    const selectedYear = Number(this.rangeSelect.value);
    const directRecords = this.findMonthlyRecords(payload);
    const directData = this.normalizeRecords(directRecords, selectedYear);

    if (directData.length) {
      return directData;
    }

    return this.normalizeParallelSeries(payload, selectedYear);
  }

  findMonthlyRecords(payload) {
    if (Array.isArray(payload)) {
      return payload;
    }

    const candidates = [
      payload?.monthlyData,
      payload?.monthlyStats,
      payload?.months,
      payload?.data,
      payload?.statistics?.monthlyData,
      payload?.stats?.monthlyData,
      payload?.result?.monthlyData,
    ];

    return candidates.find(Array.isArray) ?? [];
  }

  normalizeRecords(records, fallbackYear) {
    return records.reduce((monthlyData, record, index) => {
      const created = toFiniteNumber(resolveProperty(record, ["created", "createdTickets", "revenue", "income"]));
      const completed = toFiniteNumber(resolveProperty(record, ["completed", "completedTickets", "resolved", "expenses", "costs"]));

      if (created === null || completed === null) {
        return monthlyData;
      }

      const rawDate = resolveProperty(record, ["date", "month", "period", "label", "name"]);
      const date = createMonthDate(rawDate, fallbackYear, index);
      const label = typeof rawDate === "string" && rawDate.length <= 12
        ? rawDate
        : new Intl.DateTimeFormat("en-US", { month: "short" }).format(date);

      monthlyData.push({ label, date, created, completed });
      return monthlyData;
    }, []);
  }

  normalizeParallelSeries(payload, fallbackYear) {
    const createdSeries = resolveProperty(payload, ["monthlyCreated", "createdSeries", "created", "revenues"]);
    const completedSeries = resolveProperty(payload, ["monthlyCompleted", "completedSeries", "completed", "resolved", "expenses"]);
    const labels = resolveProperty(payload, ["labels", "months", "periods"]);

    if (!Array.isArray(createdSeries) || !Array.isArray(completedSeries)) {
      return [];
    }

    return createdSeries.reduce((monthlyData, rawCreated, index) => {
      const created = toFiniteNumber(rawCreated);
      const completed = toFiniteNumber(completedSeries[index]);

      if (created === null || completed === null) {
        return monthlyData;
      }

      const rawLabel = Array.isArray(labels) ? labels[index] : undefined;
      const date = createMonthDate(rawLabel, fallbackYear, index);
      monthlyData.push({
        label: typeof rawLabel === "string" ? rawLabel : new Intl.DateTimeFormat("en-US", { month: "short" }).format(date),
        date,
        created,
        completed,
      });
      return monthlyData;
    }, []);
  }

  renderChart() {
    const chartMetrics = this.createChartMetrics();
    this.geometry = chartMetrics;
    this.svg.replaceChildren(this.createDefinitions());
    this.renderGrid(chartMetrics);
    this.renderSeries(chartMetrics);
    this.renderCrosshair(chartMetrics);
    this.updateSummary();
  }

  createChartMetrics() {
    const bounds = {
      left: CHART_VIEWBOX.left,
      right: CHART_VIEWBOX.width - CHART_VIEWBOX.right,
      top: CHART_VIEWBOX.top,
      bottom: CHART_VIEWBOX.height - CHART_VIEWBOX.bottom,
    };
    const values = this.data.flatMap(({ created, completed }) => [created, completed]);
    const maximumValue = Math.max(...values, 10);
    const ceiling = Math.ceil((maximumValue * 1.14) / 50) * 50;
    const horizontalStep = this.data.length > 1 ? (bounds.right - bounds.left) / (this.data.length - 1) : 0;
    const scaleY = (bounds.bottom - bounds.top) / ceiling;
    const toY = (value) => bounds.bottom - value * scaleY;

    return {
      bounds,
      ceiling,
      toY,
      points: this.data.map((item, index) => ({
        index,
        x: bounds.left + horizontalStep * index,
        createdY: toY(item.created),
        completedY: toY(item.completed),
      })),
    };
  }

  createDefinitions() {
    const definitions = createSvgElement("defs");
    definitions.append(
      this.createGradient("revenue-chart-revenue-fill", "--cd-color-data-revenue", "--cd-color-data-revenue-fill"),
      this.createGradient("revenue-chart-expenses-fill", "--cd-color-data-expenses", "--cd-color-data-expenses-fill"),
      this.createGlowFilter("revenue-chart-revenue-glow"),
      this.createGlowFilter("revenue-chart-expenses-glow"),
    );
    return definitions;
  }

  createGradient(id, colorVariable, fadeVariable) {
    const gradient = createSvgElement("linearGradient", { id, x1: "0", x2: "0", y1: "0", y2: "1" });
    gradient.append(
      createSvgElement("stop", { offset: "0%", "stop-color": `var(${colorVariable})`, "stop-opacity": "0.38" }),
      createSvgElement("stop", { offset: "78%", "stop-color": `var(${fadeVariable})`, "stop-opacity": "0.05" }),
      createSvgElement("stop", { offset: "100%", "stop-color": `var(${fadeVariable})`, "stop-opacity": "0" }),
    );
    return gradient;
  }

  createGlowFilter(id) {
    const filter = createSvgElement("filter", { id, x: "-30%", y: "-30%", width: "160%", height: "160%" });
    filter.append(createSvgElement("feGaussianBlur", { stdDeviation: "3" }));
    return filter;
  }

  renderGrid({ bounds, ceiling, toY, points }) {
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

    points.forEach((point, index) => {
      const month = this.data[index];
      gridGroup.append(this.createTextLabel(month.label, point.x, bounds.bottom + 24, "revenue-chart__month-label", "middle"));
    });

    this.svg.append(gridGroup);
  }

  renderSeries({ bounds, points }) {
    const createdPoints = points.map((point) => ({ x: point.x, y: point.createdY }));
    const completedPoints = points.map((point) => ({ x: point.x, y: point.completedY }));
    const seriesGroup = createSvgElement("g", { class: "revenue-chart__series" });

    seriesGroup.append(
      this.createAreaPath(createdPoints, bounds.bottom, "revenue-chart__area revenue-chart__area--revenue", this.seriesVisibility.created),
      this.createAreaPath(completedPoints, bounds.bottom, "revenue-chart__area revenue-chart__area--expenses", this.seriesVisibility.completed),
      this.createLinePath(createdPoints, "revenue-chart__line-glow revenue-chart__line-glow--revenue", this.seriesVisibility.created),
      this.createLinePath(completedPoints, "revenue-chart__line-glow revenue-chart__line-glow--expenses", this.seriesVisibility.completed),
      this.createLinePath(createdPoints, "revenue-chart__line revenue-chart__line--revenue", this.seriesVisibility.created),
      this.createLinePath(completedPoints, "revenue-chart__line revenue-chart__line--expenses", this.seriesVisibility.completed),
    );

    points.forEach((point) => {
      seriesGroup.append(
        this.createPoint(point.index, point.x, point.createdY, "revenue"),
        this.createPoint(point.index, point.x, point.completedY, "expenses"),
      );
    });

    this.svg.append(seriesGroup);
  }

  renderCrosshair({ bounds }) {
    this.crosshair = createSvgElement("line", {
      class: "revenue-chart__crosshair",
      x1: bounds.left,
      x2: bounds.left,
      y1: bounds.top,
      y2: bounds.bottom,
    });
    this.svg.append(this.crosshair);
  }

  createAreaPath(points, baseline, className, isVisible) {
    const linePath = this.createSmoothPath(points);
    const path = createSvgElement("path", {
      class: `${className}${isVisible ? "" : " revenue-chart__series--hidden"}`,
      d: `${linePath} L ${points.at(-1).x} ${baseline} L ${points[0].x} ${baseline} Z`,
    });
    return path;
  }

  createLinePath(points, className, isVisible) {
    return createSvgElement("path", {
      class: `${className}${isVisible ? "" : " revenue-chart__series--hidden"}`,
      d: this.createSmoothPath(points),
    });
  }

  createSmoothPath(points) {
    if (!points.length) {
      return "";
    }

    if (points.length === 1) {
      return `M ${points[0].x} ${points[0].y}`;
    }

    return points.reduce((path, point, index) => {
      if (index === 0) {
        return `M ${point.x} ${point.y}`;
      }

      const previousPoint = points[index - 1];
      const beforePreviousPoint = points[index - 2] ?? previousPoint;
      const nextPoint = points[index + 1] ?? point;
      const controlPointOneX = previousPoint.x + (point.x - beforePreviousPoint.x) / 6;
      const controlPointOneY = previousPoint.y + (point.y - beforePreviousPoint.y) / 6;
      const controlPointTwoX = point.x - (nextPoint.x - previousPoint.x) / 6;
      const controlPointTwoY = point.y - (nextPoint.y - previousPoint.y) / 6;
      return `${path} C ${controlPointOneX} ${controlPointOneY}, ${controlPointTwoX} ${controlPointTwoY}, ${point.x} ${point.y}`;
    }, "");
  }

  createPoint(index, x, y, seriesName) {
    const point = createSvgElement("g", {
      class: `revenue-chart__point revenue-chart__point--${seriesName}`,
      "data-point-index": index,
      "data-series": seriesName,
    });
    point.append(
      createSvgElement("circle", { cx: x, cy: y, r: "9", fill: "none", stroke: `var(--cd-color-data-${seriesName})`, "stroke-opacity": "0.3", "stroke-width": "5" }),
      createSvgElement("circle", { class: `revenue-chart__point-core revenue-chart__point-core--${seriesName}`, cx: x, cy: y, r: "4.5" }),
    );
    return point;
  }

  createTextLabel(content, x, y, className, anchor) {
    const label = createSvgElement("text", { class: className, x, y, "text-anchor": anchor });
    label.textContent = content;
    return label;
  }

  updateSummary() {
    const latestMonth = this.data.at(-1);
    this.summary.revenue.textContent = latestMonth ? latestMonth.created.toLocaleString("en-US") + " tickets" : "—";
    this.summary.expenses.textContent = latestMonth ? latestMonth.completed.toLocaleString("en-US") + " tickets" : "—";
  }

  handlePointerMove(event) {
    if (!this.geometry?.points.length) {
      return;
    }

    const svgRect = this.svg.getBoundingClientRect();
    const pointerX = event.clientX - svgRect.left;
    const proportion = Math.min(1, Math.max(0, pointerX / svgRect.width));
    const index = Math.round(proportion * (this.geometry.points.length - 1));
    this.showTooltip(index);
  }

  handleKeyboardNavigation(event) {
    if (!this.data.length) {
      return;
    }

    const currentIndex = this.activeIndex ?? 0;
    let nextIndex = currentIndex;

    if (event.key === "ArrowRight") {
      nextIndex = Math.min(currentIndex + 1, this.data.length - 1);
    } else if (event.key === "ArrowLeft") {
      nextIndex = Math.max(currentIndex - 1, 0);
    } else if (event.key === "Home") {
      nextIndex = 0;
    } else if (event.key === "End") {
      nextIndex = this.data.length - 1;
    } else if (event.key === "Escape") {
      this.hideTooltip();
      return;
    } else {
      return;
    }

    event.preventDefault();
    this.showTooltip(nextIndex);
  }

  showTooltip(index) {
    const point = this.geometry.points[index];
    const dataPoint = this.data[index];
    const previousDataPoint = this.data[index - 1];
    const growth = previousDataPoint && previousDataPoint.created !== 0
      ? ((dataPoint.created - previousDataPoint.created) / previousDataPoint.created) * 100
      : null;

    this.activeIndex = index;
    this.crosshair.setAttribute("x1", point.x);
    this.crosshair.setAttribute("x2", point.x);
    this.crosshair.classList.add("is-visible");
    this.svg.querySelectorAll(".revenue-chart__point").forEach((node) => {
      node.classList.toggle("is-active", Number(node.dataset.pointIndex) === index);
    });

    this.tooltipFields.date.textContent = new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" }).format(dataPoint.date);
    this.tooltipFields.revenue.textContent = dataPoint.created.toLocaleString("en-US") + " tickets";
    this.tooltipFields.expenses.textContent = dataPoint.completed.toLocaleString("en-US") + " tickets";
    this.tooltipFields.growth.textContent = growth === null ? "No prior month" : `${growth >= 0 ? "+" : ""}${growth.toFixed(1)}% volume`;

    const plotWidth = this.plot.clientWidth;
    const plotHeight = this.plot.clientHeight;
    const xPercent = point.x / CHART_VIEWBOX.width;
    const yPercent = Math.min(point.createdY, point.completedY) / CHART_VIEWBOX.height;
    const tooltipWidth = 216;
    const x = Math.min(plotWidth - tooltipWidth / 2, Math.max(tooltipWidth / 2, xPercent * plotWidth));
    const y = Math.max(74, yPercent * plotHeight);
    this.tooltip.style.left = `${x}px`;
    this.tooltip.style.top = `${y}px`;
    this.tooltip.setAttribute("aria-hidden", "false");
    this.tooltip.classList.add("is-visible");
  }

  hideTooltip() {
    this.activeIndex = null;
    this.crosshair?.classList.remove("is-visible");
    this.svg?.querySelectorAll(".revenue-chart__point").forEach((node) => node.classList.remove("is-active"));
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