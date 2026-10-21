import { apiRequest } from "../config/http-client.js";
import { listUsers, deleteUser } from "./user.service.js";
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

  const usersByUsername = new Map();
  for (const u of allUsers) {
    if (u?.username) {
      usersByUsername.set(u.username.toLowerCase(), u);
    }
  }

  // Collect all unique technician usernames from both users and profile queries
  const technicianUsernames = new Set();
  for (const u of allUsers) {
    const role = String(u.role || "").toUpperCase();
    if (role === "TECHNICIAN" || role.includes("TECH") || role.includes("TECNICO")) {
      technicianUsernames.add(u.username.toLowerCase());
    }
  }
  for (const p of techProfiles) {
    if (p?.username) {
      technicianUsernames.add(p.username.toLowerCase());
    }
  }

  return Array.from(technicianUsernames).map((lower) => {
    const u = usersByUsername.get(lower);
    const profile = profilesByUsername.get(lower);
    const username = u?.username || profile?.username || lower;
    const email = u?.email || "—";
    const role = u?.role || "TECHNICIAN";
    const count = typeof profile?.activeTicketsCount === "number" ? profile.activeTicketsCount : 0;

    let availability = profile?.availabilityStatus;
    if (!availability) {
      if (u?.disabled || u?.locked) {
        availability = "OFFLINE";
      } else if (count > 0) {
        availability = "BUSY";
      } else {
        availability = "AVAILABLE";
      }
    }

    return {
      username,
      email,
      role,
      specialty: profile?.specialty || "Soporte General",
      activeTicketsCount: count,
      availabilityStatus: availability,
      locked: Boolean(u?.locked),
      disabled: Boolean(u?.disabled),
    };
  });
}

/**
 * Deletes a technician by username.
 * @param {string} username
 * @returns {Promise<void>}
 */
export function deleteTechnician(username) {
  return deleteUser(username);
}
