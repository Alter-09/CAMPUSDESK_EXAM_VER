/**
 * Carga las hojas de estilo dentro de un Shadow DOM.
 * El Shadow DOM aísla los estilos, así que se enlaza el mismo main.css de la página
 * (para reutilizar .field y .button), el tema de los componentes y la hoja propia.
 */

const STYLESHEETS_BASE = new URL("../../stylesheet/", import.meta.url);

const SHARED_STYLESHEETS = [
  new URL("main.css", STYLESHEETS_BASE),
  new URL("components/shadow-theme.css", STYLESHEETS_BASE),
];

/**
 * @param {URL} url
 * @returns {{ element: HTMLLinkElement, loaded: Promise<void> }}
 */
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

/**
 * @param {ShadowRoot} root
 * @param {string} componentStylesheet Archivo dentro de stylesheet/components/, ej. "user-list.css".
 * @returns {Promise<void>} Resuelve cuando las hojas cargaron (o fallaron),
 *   para pintar el contenido ya con estilos y evitar el parpadeo.
 */
export function attachStyles(root, componentStylesheet) {
  const urls = [...SHARED_STYLESHEETS, new URL(`components/${componentStylesheet}`, STYLESHEETS_BASE)];
  const links = urls.map(createStyleLink);

  root.append(...links.map(({ element }) => element));

  return Promise.all(links.map(({ loaded }) => loaded)).then(() => undefined);
}
