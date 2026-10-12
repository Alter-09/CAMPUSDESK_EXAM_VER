import { RevenueChart } from "../components/revenue-chart.js";

function initializeSuperadminDashboard() {
  const chartHost = document.querySelector("[data-revenue-chart]");

  if (!chartHost) {
    return;
  }

  const revenueChart = new RevenueChart(chartHost);
  revenueChart.mount().catch((error) => {
    console.error("Unable to initialize the revenue chart.", error);
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initializeSuperadminDashboard, { once: true });
} else {
  initializeSuperadminDashboard();
}
