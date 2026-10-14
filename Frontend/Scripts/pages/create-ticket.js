import { initUserPage } from "../utils/user-page.utils.js";

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initUserPage, { once: true });
} else {
  initUserPage();
}
