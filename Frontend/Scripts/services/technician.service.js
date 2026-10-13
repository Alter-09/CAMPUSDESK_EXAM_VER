/**
 * Servicio para gestión y consulta de técnicos.
 * Sigue principios SOLID (Single Responsibility, Dependency Inversion con http-client).
 */
import { apiRequest } from "../config/http-client.js";
import { listUsers, deleteUser } from "./user.service.js";

/**
 * @typedef {{
 *   username: string,
 *   email: string,
 *   role: string,
 *   specialty?: string,
 *   availabilityStatus?: string,
 *   activeTicketsCount?: number,
 *   locked?: boolean,
 *   disabled?: boolean,
 * }} TechnicianItem
 */

/**
 * Consulta la lista unificada de técnicos del sistema.
 * Combina /api/user/all (usuarios con rol TECHNICIAN) con /api/technicians (perfiles y carga de tiquetes).
 * @returns {Promise<TechnicianItem[]>}
 */
export async function listTechnicians() {
  const [usersResult, techResult] = await Promise.allSettled([
    listUsers(),
    apiRequest("/api/technicians"),
  ]);

  const allUsers = usersResult.status === "fulfilled" && Array.isArray(usersResult.value)
    ? usersResult.value
    : [];

  const techProfiles = techResult.status === "fulfilled" && Array.isArray(techResult.value)
    ? techResult.value
    : [];

  const profilesByUsername = new Map();
  for (const profile of techProfiles) {
    if (profile?.username) {
      profilesByUsername.set(profile.username.toLowerCase(), profile);
    }
  }

  // Filtrar usuarios cuyo rol sea TECHNICIAN o contenga TECH
  const technicianUsers = allUsers.filter((u) => {
    const role = String(u.role || "").toUpperCase();
    return role === "TECHNICIAN" || role.includes("TECH") || role.includes("TECNICO");
  });

  // Mapear y enriquecer con los datos de TechnicianProfile si existen
  return technicianUsers.map((u) => {
    const profile = profilesByUsername.get(u.username.toLowerCase());
    const count = typeof profile?.activeTicketsCount === "number" ? profile.activeTicketsCount : 0;
    
    let availability = profile?.availabilityStatus;
    if (!availability) {
      if (u.disabled || u.locked) {
        availability = "OFFLINE";
      } else if (count > 0) {
        availability = "BUSY";
      } else {
        availability = "AVAILABLE";
      }
    }

    return {
      username: u.username,
      email: u.email || "—",
      role: u.role || "TECHNICIAN",
      specialty: profile?.specialty || "Soporte General",
      activeTicketsCount: count,
      availabilityStatus: availability,
      locked: Boolean(u.locked),
      disabled: Boolean(u.disabled),
    };
  });
}

/**
 * Elimina un técnico por su nombre de usuario.
 * @param {string} username
 * @returns {Promise<void>}
 */
export function deleteTechnician(username) {
  return deleteUser(username);
}
