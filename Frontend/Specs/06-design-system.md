# 06 · Sistema de Diseño — "Titan Meridian" aplicado a CampusDesk

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Fuente raíz | `DESIGN.md` · sha256 (primeros 16) `86a387eff03273ad` — si el hash cambia, revisar este archivo **antes** de tocar CSS (AUD-14) |
| Depende de | `00-constitution.md` (C-01, C-13, C-15) |
| Alcance | Identidad visual raíz (color, tipografía, espaciado, elevación, componentes base). Las **pantallas** y los **assets** vienen de Figma (`Q-09`) |

> `DESIGN.md` es en inglés porque es el documento de marca; la **interfaz** de CampusDesk sigue en **español** (C-03).

---

## 1. Principios de marca (resumen)

Tema **oscuro** de azul marino abisal con un único acento **coral carmín** usado con intención (acción primaria, alertas, cursores de línea de tiempo, KPI activos). Geometría nítida (radios contenidos), profundidad por **capas tonales** (no sombras pesadas), números tabulares, y lectura de alto contraste para métricas críticas.

## 2. Tokens de color — conjunto de marca (candidato a fuente de verdad, `Q-33`)

Nomenclatura: `--cd-color-<nombre-literal-de-DESIGN.md>` (trazabilidad 1:1, sin renombrar).

| Token (`DESIGN.md`) | Custom property | Valor | Uso según la prosa |
|---|---|---|---|
| `surface-canvas` | `--cd-color-surface-canvas` | `#010e24` | Nivel 0 · fondo base · fondo de inputs y cabecera de tabla |
| `surface-navy-deep` | `--cd-color-surface-navy-deep` | `#011e44` | Nivel 1 · contenedores estructurales |
| `surface-card` | `--cd-color-surface-card` | `#062654` | Nivel 2 · tarjetas/paneles activos |
| `surface-elevated` | `--cd-color-surface-elevated` | `#0c326d` | Nivel 3 · popovers y flotantes |
| `signal-coral` | `--cd-color-signal-coral` | `#e75c69` | CTA primario, alertas, foco, KPI activo |
| `signal-coral-glow` | `--cd-color-signal-coral-glow` | `#ff6b79` | Hover del CTA |
| `signal-coral-muted` | `--cd-color-signal-coral-muted` | `rgba(231,92,105,.15)` | Relleno de chip crítico |
| `slate-titanium-100` | `--cd-color-slate-titanium-100` | `#f1f5f9` | Texto principal (inputs, hover ghost) |
| `slate-titanium-300` | `--cd-color-slate-titanium-300` | `#cbd5e1` | Texto secundario, botón secundario |
| `slate-titanium-500` | `--cd-color-slate-titanium-500` | `#64748b` | Placeholder, botón ghost, metadatos |
| `border-subtle` | `--cd-color-border-subtle` | `rgba(203,213,225,.12)` | Bordes estructurales |
| `border-active` | `--cd-color-border-active` | `rgba(231,92,105,.4)` | Bordes activos |
| `data-metric-green` | `--cd-color-data-metric-green` | `#10b981` | Nominal / tendencia positiva |
| `data-metric-amber` | `--cd-color-data-metric-amber` | `#f59e0b` | **Sin uso definido en la prosa** (`Q-37`) |

**Conjunto B (roles tipo Material 3 en el frontmatter):** `surface`, `background` (`#0a1324`), `primary` (`#b0c7f6`), `secondary` (`#ffb3b5`), `tertiary`, `error` (`#ffb4ab`), `on-*`, `*-container`, `*-fixed`, etc. **No se usa ninguno hasta resolver `Q-33`**, porque contradice a la prosa (p. ej. fondo `#0a1324` vs. canvas `#010e24`; acción primaria coral vs. `primary` azul claro).

### 2.1 Valores usados en la prosa **sin token** (`Q-33`) 🔶

| Valor | Dónde aparece | Nombre propuesto |
|---|---|---|
| `#022450` | Fila alterna de tabla | `--cd-color-surface-row-alt` |
| `rgba(203,213,225,.08)` | Borde del Nivel 1 | `--cd-color-border-level-1` |
| `rgba(203,213,225,.06)` | Divisor de filas · hover del botón secundario | `--cd-color-border-row` / `--cd-color-hover-subtle` |
| `rgba(203,213,225,.16)` | Borde de input · borde de popover | `--cd-color-border-input` |
| `rgba(203,213,225,.24)` → `.45` | Borde del botón secundario → hover | `--cd-color-border-button` / `--cd-color-border-button-hover` |
| `rgba(6,38,84,.5)` | Fondo hover del botón ghost | `--cd-color-hover-ghost` |
| `rgba(231,92,105,.08)` | Hover de fila | `--cd-color-row-hover` |
| `rgba(231,92,105,.2)` | Anillo de foco (3 px) | `--cd-color-focus-ring` |
| `rgba(231,92,105,.25)` | Resalte superior del Nivel 2 | `--cd-color-card-highlight` |
| `rgba(231,92,105,.35)` | Resplandor hover del CTA | `--cd-color-glow` |
| `rgba(231,92,105,.3)` | Anillo de elevación "señal" | `--cd-color-signal-ring` |
| `rgba(203,213,225,.1)` | Fondo chip neutro | `--cd-color-chip-neutral` |
| `rgba(16,185,129,.15)` | Fondo chip nominal | `--cd-color-chip-nominal` |
| `#ffffff` | Texto del CTA primario | `--cd-color-on-signal` |

> Regla (AUD-14): ningún valor de color puede escribirse fuera de `tokens.css`.

## 3. Tipografía

| Estilo | Familia | Tamaño / interlínea | Peso | Tracking | Uso |
|---|---|---|---|---|---|
| `display-hero` | Hanken Grotesk | 48 / 56 | 700 | −0.03em | Títulos héroe |
| `display-hero-mobile` | Hanken Grotesk | 32 / 40 | 700 | −0.02em | Idem en <768 px |
| `headline-lg` | Hanken Grotesk | 32 / 40 | 600 | −0.02em | Título de página |
| `headline-lg-mobile` | Hanken Grotesk | 24 / 32 | 600 | −0.01em | Idem en <768 px |
| `headline-md` | Hanken Grotesk | 24 / 32 | 600 | −0.01em | Secciones |
| `headline-sm` | Hanken Grotesk | 18 / 26 | 600 | 0 | Título de tarjeta |
| `metric-display` | Hanken Grotesk | 36 / 44 | 700 | −0.03em | Valor del KPI |
| `body-lg` | Inter | 16 / 24 | 400 | 0 | Texto narrativo |
| `body-md` | Inter | 14 / 20 | 400 | 0 | Texto de interfaz, inputs |
| `body-sm` | Inter | 12 / 18 | 400 | 0.01em | Ayudas, metadatos |
| `label-mono-sm` | JetBrains Mono | 11 / 16 | 500 | 0.06em | Timestamps, valores crudos |
| `label-mono-xs` | JetBrains Mono | 10 / 14 | 600 | 0.08em | Chips, cabeceras de tabla |

**Reglas de implementación**
- Unidades en **`rem`** (1 rem = 16 px) para respetar el zoom/escala del usuario; no fijar `html { font-size }` en px.
- Cifras **tabulares** en todos los números: `font-variant-numeric: tabular-nums` (`DESIGN.md` exige `tnum`; verificar soporte en la build autoalojada de Hanken Grotesk — `Q-39`).
- Mayúsculas de etiquetas mono con `text-transform: uppercase` (CSS), **no** escribiendo el texto en mayúsculas.
- Variantes `-mobile` se activan por media query (<768 px); los demás estilos no tienen variante móvil definida.
- Un estilo = una clase utilitaria (`.cd-text-headline-lg`, …) definida una sola vez en `typography.css` 🔶.
- Pilas de respaldo (necesarias, no definidas en `DESIGN.md`) 🔶: `system-ui, -apple-system, "Segoe UI", sans-serif` y `ui-monospace, SFMono-Regular, Menlo, Consolas, monospace`.

## 4. Espaciado, grilla y breakpoints

Ritmo base **8 px** con micropasos de 4 px.

| Token | Valor | | Token | Valor |
|---|---|---|---|---|
| `space-xs` | 0.25rem (4) | | `gutter` | 1.25rem (20) |
| `space-sm` | 0.5rem (8) | | `gutter-lg` | 1.5rem (24) |
| `space-md` | 1rem (16) | | `margin` | 1rem (16) |
| `space-lg` | 1.5rem (24) | | `margin-md` | 1.5rem (24) |
| `space-xl` | 2rem (32) | | `margin-lg` | 2rem (32) |
| `space-2xl` | 3rem (48) | | | |

| Viewport | Columnas | Gutter | Margen | Navegación |
|---|---|---|---|---|
| **Desktop ≥1280** | 12 | 24 (`gutter-lg`) | 32 (`margin-lg`) | Riel lateral fijo/colapsable: 64 px mini · 260 px expandido |
| **Tablet 768–1279** | 8 | 20 (`gutter`) | 24 (`margin-md`) | Drawer off-canvas |
| **Mobile <768** | 4 | 16 (**sin token**; coincide con `space-md`) | 16 (`margin`) | Columna única; tablas con scroll horizontal (detalle del menú: `Q-36`) |

✅ Los breakpoints quedan **confirmados** por `DESIGN.md` (resuelve la parte "responsive" de `Q-26`).

## 5. Radios y elevación

| Token | Valor | Uso |
|---|---|---|
| `sm` / `DEFAULT` / `md` / `lg` / `xl` / `full` | 2 / 4 / 6 / 8 / 12 px / 9999 | 4 = micro-elementos · 8 = tarjetas y botones · 12 = marco de modales · `full` solo para el punto de estado de 6 px (los badges **no** son píldora) |

| Nivel | Superficie | Tratamiento |
|---|---|---|
| 0 · Suelo | `surface-canvas` | Sólido |
| 1 · Contenedor | `surface-navy-deep` | Borde 1 px `rgba(203,213,225,.08)` |
| 2 · Tarjeta/panel activo | `surface-card` | Borde superior 1 px `rgba(231,92,105,.25)` |
| 3 · Flotante | `surface-elevated` | `0 12px 32px -4px rgba(1,14,36,.7), 0 0 0 1px rgba(203,213,225,.15)` |
| Señal (modal fijado / urgente) | — | Anillo 1 px `signal-coral` al 30 % |

## 6. Fuentes autoalojadas (C-01) — `Q-39`

| Familia | Pesos requeridos por `DESIGN.md` | Estilos que los usan |
|---|---|---|
| Hanken Grotesk | **600, 700** | headline-*, display-*, metric-display |
| Inter | **400, 600** (600 = texto del botón) | body-*, botones, labels de formulario |
| JetBrains Mono | **500, 600** | label-mono-* |

- Formato **WOFF2**, subconjuntos **latin + latin-ext** (el español usa á é í ó ú ñ ü ¿ ¡).
- `@font-face` en `css/base/fonts.css` con `font-display: swap`; `<link rel="preload" as="font" type="woff2" crossorigin>` solo para las críticas (Inter 400, Hanken 600/700).
- Licencia: se entiende que las tres son de código abierto (SIL OFL) — **confirmar en el origen de descarga** y conservar el archivo de licencia en `assets/fonts/`.
- Las fuentes se obtienen **una vez** (descarga manual de Sebbs o paquete de fuentes bajado en desarrollo) y se versionan en el repo; **no** se enlazan desde ningún CDN en tiempo de ejecución.
- Variable vs. estáticas, y si es necesario subconjunto propio: decisión pendiente en `Q-39`.

## 7. Componentes base de `DESIGN.md` → implementación

| Componente | Especificación de `DESIGN.md` | Hueco / decisión pendiente |
|---|---|---|
| **Botón primario** | Fondo `signal-coral`, texto `#fff`, radio 8, Inter 14/600. Hover: `signal-coral-glow` + resplandor `0 0 12px`. Activo: `scale(.98)` | Foco, `disabled`, `loading` no definidos · contraste (H-01) |
| **Botón secundario** | Transparente, borde `.24`, texto `titanium-300`; hover: fondo `.06` y borde `.45` | Foco, `disabled`, `loading` |
| **Botón ghost** | Sin borde, texto `titanium-500`; hover: texto `titanium-100` + fondo `rgba(6,38,84,.5)` | Contraste (H-02) |
| **Tarjeta / KPI** | Fondo navy-deep o card, radio 8, borde sutil; cabecera `headline-sm` + control segmentado de rango opcional; métrica `metric-display`; badge de tendencia mono 11; pie con sparkline | Tendencia y sparkline sin datos de API (`Q-38`) |
| **Tabla** | Cabecera sticky, fondo canvas, `label-mono-xs` mayúsculas; filas alternas `#011e44`/`#022450`; divisor `.06`; hover coral `.08` + acciones rápidas | Acciones solo en hover → ver H-08 |
| **Input / select** | Alto 40, padding `0 12`, fondo canvas, borde `.16`, texto `titanium-100`, placeholder `titanium-500`; foco: borde coral + `0 0 0 3px` `.2` | Estado error, `disabled`, `textarea`, `select`, checkbox/radio no definidos (`Q-36`) |
| **Chips / badges** | Alto 22, `label-mono-xs` mayúsculas · neutro / crítico (+ punto pulsante 6 px) / nominal | Mapeo estado/prioridad/categoría → variante (`Q-37`) |
| **Gráficas** | Rejilla discontinua `.08`, serie coral 2.5 px con degradado, cursor vertical 1 px, tooltip nivel 3 | Fuera de alcance salvo que el backend entregue series (`Q-38`); sin librerías → SVG propio |

**No definidos en `DESIGN.md` (necesarios para CampusDesk):** modal/diálogo de confirmación · toast · paginación · timeline de historial · hilo de comentarios · skeleton de carga · estados vacíos · páginas de error · encabezado y menú de usuario · ítems del riel de navegación · menú móvil → vienen de Figma o se preguntan (`Q-36`).

## 8. Hallazgos de la auditoría de diseño

Método: contraste WCAG 2.x (luminancia relativa sRGB); las transparencias se componen sobre la superficie real. Umbrales: texto normal 4.5:1 (AA) · 7:1 (AAA) · componentes UI y foco 3:1.

| ID | Hallazgo | Medición | Impacto | Decisión |
|---|---|---|---|---|
| **H-01** | Texto blanco sobre el **botón primario** | `#fff` / `#e75c69` = **3.42:1**; en hover (`#ff6b79`) = **2.75:1** | Falla AA (texto 14 px/600 no es "grande"); el hover empeora y no llega ni a 3:1 | `Q-34` — alternativa medida: texto `#010e24` sobre coral = 5.63:1 |
| **H-02** | `slate-titanium-500` como **texto** (placeholder, ghost, metadatos) | sobre canvas **4.05** · navy **3.47** · card **3.12** · elevated **2.61** | Falla AA en todas las superficies | `Q-34` |
| **H-03** | **Coral como texto** y chip crítico | coral/canvas 5.63 ✅ · navy 4.83 ✅ · card **4.34** ❌ · elevated **3.62** ❌ · texto del chip crítico sobre su relleno: **4.19** (navy) / **3.79** (card) ❌ | Falla AA en tarjeta/elevado y en chips de 10 px | `Q-34` |
| **H-04** | **Borde del input** (16 %) como único delimitador | Borde vs. su fondo **1.41:1**; vs. la tarjeta que lo contiene **1.09:1** | No llega a 3:1 (WCAG 1.4.11): el campo se distingue mal en reposo | `Q-34` |
| **H-05** | Anillo de **foco** coral | ≥ 4.34:1 sobre todas las superficies | Cumple ✅ | — |
| **H-06** | Texto `titanium-100/300`, verde, ámbar, `error #ffb4ab` | titanium ≥ 8.36 · verde 4.89–7.60 · ámbar 5.78–8.97 · error 7.31–11.35 | Cumple AA/AAA ✅ (el "AAA para métricas críticas" de `DESIGN.md` es cierto **para ese texto**, no para todo el sistema) | Informa `Q-27` |
| **H-07** | Color de **error de formulario** no definido | La prosa usa coral para alertas; el frontmatter solo trae `error #ffb4ab` | Hace falta para validaciones (C-11) | `Q-33`/`Q-34` |
| **H-08** | Acciones rápidas de fila **solo en hover** | — | Inaccesibles por teclado y táctil | Mostrar también con `:focus-within` y alternativa táctil — `Q-36` |
| **H-09** | Animaciones: punto pulsante, `scale(.98)`, resplandores | — | Deben respetar `prefers-reduced-motion` | Requisito (C-13) |
| **H-10** | Inconsistencias menores | Borde nivel 1 `.08` vs `border-subtle` `.12` · gutter móvil 16 vs token 20 · `full` (píldora) vs "badges sin píldora" · dos conjuntos de colores | Riesgo de implementar valores distintos | `Q-33` |
| **H-11** | Datos que el diseño supone y la API no entrega | Tendencia %, sparkline, gráficas de series | Diseño sin respaldo | `Q-38` |
| **H-12** | Cifras tabulares y etiquetas de 10–11 px | `tnum` exigido; mono 10 px | Verificar `tnum` en Hanken Grotesk autoalojada y legibilidad real | `Q-39` |

> Ningún color se modifica en este documento: son decisiones de diseño de Sebbs. Hasta resolver `Q-34`, la implementación usa los valores **tal cual** y registra cada desviación como deuda técnica **S3 con dueño** (`04-phases.md` §6).

## 9. Insumos pendientes (Sebbs)

Además de los de `02-plan.md` §12: confirmar jerarquía de tokens (`Q-33`), decisiones de contraste (`Q-34`), tema único (`Q-35`), componentes faltantes (`Q-36`), mapeo de chips (`Q-37`), datos de KPI (`Q-38`) y archivos de fuentes (`Q-39`).

**Mapeo de chips a definir (`Q-37`)**

| Valor | Variante (neutro / crítico / nominal / otra) |
|---|---|
| ABIERTA · ASIGNADA · EN_PROCESO · RESUELTA · CERRADA | ❓ |
| BAJA · MEDIA · ALTA · CRITICA | ❓ |
| HARDWARE · SOFTWARE · REDES · ACCESOS · OTROS | ❓ |

## 10. Plan de implementación de tokens 🔶

1. `css/base/tokens.css`: custom properties literales (§2, §4, §5) en `:root`; **único** lugar con valores crudos.
2. `css/base/fonts.css` + `typography.css` (clases `.cd-text-*`, variantes móviles por media query).
3. Los Web Components heredan los tokens (las custom properties atraviesan el Shadow DOM) y adoptan hojas compartidas.
4. **AUD-14:** (a) comparar hash de `DESIGN.md`; (b) buscar hex/rgba fuera de `tokens.css` (`grep -rEn "#[0-9a-fA-F]{3,8}|rgba?\(" css components --include=*.css | grep -v tokens.css` debe quedar vacío); (c) revisión visual contra Figma.

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial a partir de `DESIGN.md` (sha256 `86a387eff03273ad`) |
