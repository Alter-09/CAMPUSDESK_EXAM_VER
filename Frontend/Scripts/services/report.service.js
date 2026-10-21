import { API_BASE_URL, TOKEN_STORAGE_KEY } from "../config/api.config.js";

// [Nueva Funcionalidad: U-19/A-11 - Servicio Frontend de Descarga de Reportes] - Descarga de tickets en formato CSV - Conecta con: /api/v1/reports/my-tickets/export
export function downloadMyTicketsCsv() {
  const token = sessionStorage.getItem(TOKEN_STORAGE_KEY);
  const url = `${API_BASE_URL}/api/v1/reports/my-tickets/export`;

  fetch(url, {
    method: "GET",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
    .then((res) => {
      if (!res.ok) throw new Error("Error al descargar reporte");
      return res.blob();
    })
    .then((blob) => {
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = "mis_tickets.csv";
      document.body.appendChild(a);
      a.click();
      a.remove();
    })
    .catch((err) => console.error("Error al exportar CSV:", err));
}
