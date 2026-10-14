const STYLESHEETS_BASE = new URL("../../stylesheet/", import.meta.url);

const SHARED_STYLESHEETS = [
  new URL("main.css", STYLESHEETS_BASE),
  new URL("components/shadow-theme.css", STYLESHEETS_BASE),
];
function createStyleLink(url) {
  const element = document.createElement("link");
  element.rel = "stylesheet";
  element.href = url.href;

  const loaded = new Promise((resolve) => {
    element.addEventListener("load", () => resolve(), { once: true });
    element.addEventListener("error", () => resolve(), { once: true });
  });

  return { element, loaded };
}
export function attachStyles(root, componentStylesheet) {
  const urls = [...SHARED_STYLESHEETS, new URL(`components/${componentStylesheet}`, STYLESHEETS_BASE)];
  const links = urls.map(createStyleLink);

  root.append(...links.map(({ element }) => element));

  return Promise.all(links.map(({ loaded }) => loaded)).then(() => undefined);
}
