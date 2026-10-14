import { MESSAGES } from "../constants/messages.es.js";
export function toUserView(user, roles) {
  const role = roles.find((item) => item.name === user.role);
  const rolePermissions = role?.permissions ?? [];
  const additionalPermissions = user.additionalPermissions ?? [];

  return {
    username: user.username,
    roleName: user.role,
    roleLabel: MESSAGES.users.roleLabels[user.role] ?? user.role,
    permissions: [...new Set([...rolePermissions, ...additionalPermissions])].sort(),
  };
}
export function toUserViews(users, roles) {
  return users.map((user) => toUserView(user, roles));
}
