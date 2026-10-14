import { MESSAGES } from "../constants/messages.es.js";
import { USER_ADD_REQUESTED_EVENT } from "../components/user-list.js";
import { USER_CREATED_EVENT } from "../components/user-form-modal.js";

function initNewUserPage() {
  const list = document.querySelector("cd-user-list");
  const modal = document.querySelector("cd-user-form-modal");

  if (!list || !modal) {
    return;
  }

  list.addEventListener(USER_ADD_REQUESTED_EVENT, () => modal.open());

  modal.addEventListener(USER_CREATED_EVENT, async (event) => {
    await list.reload();
    list.notify(MESSAGES.users.list.created(event.detail.username));
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initNewUserPage, { once: true });
} else {
  initNewUserPage();
}
