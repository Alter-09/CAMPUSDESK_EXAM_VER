# 00 · Constitución — CampusDesk Frontend

> Reglas **no negociables**. Si una spec, un plan o una tarea las contradice, **gana este documento**. Solo Sebbs puede modificarlas (dejar rastro en el historial).

| Campo | Valor |
|---|---|
| Versión | 0.1.0 |
| Fecha | 2026-10-08 |
| Depende de | — |

---

## 1. Alcance y rol

- Este paquete cubre **solo el frontend**. Backend (Spring Boot, PostgreSQL, JWT, Swagger, CORS) pertenece al equipo backend.
- El frontend **consume** la API. Nunca sustituye la validación ni las reglas de negocio del servidor (RF-04: *"no es suficiente con restringir las acciones desde la interfaz"*).

## 2. Reglas inquebrantables

| ID | Regla | Cómo se verifica |
|---|---|---|
| **C-01** | **Stack cerrado:** HTML5 + CSS3 + JavaScript vanilla (módulos ES). Prohibido: frameworks, librerías JS, frameworks CSS, CDNs, iconos/fuentes remotas (incluye Google Fonts), dependencias `npm` de ejecución. Las fuentes de `DESIGN.md` se **autoalojan** en `assets/fonts/`. | AUD-01 |
| **C-02** | **Separación estricta:** HTML = estructura · CSS = presentación · JS = comportamiento. Prohibido `<style>`, atributo `style=""`, handlers `on*=""`, JS embebido y CSS/HTML mezclado en un mismo archivo (incluye plantillas de Web Components — ver Q-11). | AUD-02 |
| **C-03** | **Idioma:** TODO el código en **inglés** (variables, funciones, clases, archivos, carpetas, clases CSS, ids, eventos, claves). **Comentarios y JSDoc en español.** Todo texto visible por el cliente en **español**, centralizado en `js/constants/messages.es.js` (el texto estático del HTML también va en español). | AUD-03 |
| **C-04** | **SOLID** aplicado según `02-plan.md` §4. | AUD-04 |
| **C-05** | **DRY — una sola fuente de verdad** para: endpoints, enums y sus etiquetas, reglas de validación, tokens de diseño, mapeo de errores HTTP, matriz rol→UI. Duplicar una de estas = hallazgo **S2**. | AUD-04 |
| **C-06** | **Código limpio:** nombres que revelan intención; funciones de un solo propósito (guía ≤ 25 líneas, revisar si > 40); archivos por responsabilidad (guía ≤ 250 líneas); sin números/strings mágicos; sin código muerto; sin `var`; `===` siempre; sin variables globales en `window`; sin `console.log` en la entrega. | AUD-04, AUD-05 |
| **C-07** | **Asincronía:** toda llamada a la API con `async/await`; errores tratados en la capa HTTP; sin cadenas `.then()`; sin promesas sin manejar. | AUD-05 |
| **C-08** | **Seguridad en cliente:** nunca `innerHTML` con datos de usuario/API (usar `textContent` y APIs DOM); el token solo lo toca `SessionManager`; token jamás en URL ni logs; sin secretos en el repo; mensajes de error sin datos sensibles. | AUD-12 |
| **C-09** | **El backend es la autoridad:** la UI oculta/deshabilita acciones por UX, pero **siempre** maneja 400/401/403/404/409/500 aunque no haya ofrecido la acción. | AUD-08 |
| **C-10** | **Sin datos simulados en la entrega final** (Restricción 11 del enunciado). Los mocks viven en `mocks/`, solo en desarrollo, tras `API_MODE`, y se desactivan/eliminan en la Fase 9. | AUD-13 |
| **C-11** | **Formularios:** validar en JS **antes** de llamar a la API, mostrando explícitamente **qué falta o qué está mal** por campo (+ resumen); conservar lo digitado ante error. | AUD-07 |
| **C-12** | **UX mínima por vista:** estados de carga, vacío, error y éxito; botones deshabilitados durante el envío; confirmación en acciones irreversibles (p. ej. cerrar tiquete). | AUD-06 |
| **C-13** | **Accesibilidad mínima:** HTML semántico, `label` en todo control, foco visible y gestionado, uso solo con teclado, errores anunciados (`aria-live`), `prefers-reduced-motion` respetado. Nivel objetivo y contrastes: **Q-27 / Q-34**. | AUD-09 |
| **C-14** | **Consumo eficiente de la API:** filtros y paginación en el servidor, sin llamadas duplicadas ni innecesarias, cancelación de peticiones obsoletas, caché con invalidación (`02-plan.md` §10). | AUD-10 |
| **C-15** | **Fidelidad al diseño — jerarquía de fuentes:** (1) `DESIGN.md` "Titan Meridian" = identidad raíz (tokens, tipografía, componentes base); (2) Figma de Sebbs = pantallas, estructura y assets; (3) lo que ninguno defina **se pregunta**, no se inventa. | AUD-14 |
| **C-16** | **Fases con puertas de calidad:** no se avanza con hallazgos S1/S2 abiertos (`04-phases.md`). | Gate de cada fase |

---

## 3. Protocolo de Cero Suposiciones

> Objetivo: eliminar alucinaciones y gastar menos tokens. **Preguntar es siempre más barato que rehacer.**

### 3.1 Antes de escribir una sola línea de código

1. Identificar la tarea (`Fx-Txx`) y la sección de spec que la respalda.
2. Verificar que **todo dato necesario esté ✅**. Datos 🔶 o ❓ **no se implementan**.
3. Si falta algo → **detenerse y preguntar** (formato 3.3). No rellenar con "lo típico".
4. Registrar la pregunta en `05-questions-and-decisions.md`; bloquear **solo** lo afectado y continuar con lo no bloqueado.
5. Al llegar la respuesta: actualizar `05` → actualizar spec/plan/contrato afectado → **recién** implementar.

### 3.2 Qué cuenta como suposición (ejemplos prohibidos)

Inventar: nombres de campos o endpoints · colores, tipografías, espaciados o iconos · textos de la interfaz · límites de validación · comportamientos por rol no documentados · formato de fechas · estructura de paginación · formato de errores del backend · qué hace un botón que el diseño no define.

### 3.3 Formato de una pregunta (máx. 6 líneas)

```
Q-xx · Contexto (1 línea) · Opciones A/B (con costo de cada una) · Impacto · Qué queda bloqueado
```

### 3.4 Reglas para el asistente de código (IA)

- Responder en español; código en inglés; comentarios en español.
- **Explicar el porqué de una decisión de diseño antes de escribir el código** (Sebbs prioriza entender el razonamiento).
- Trabajar en **pasos pequeños y verificables**; no avanzar al siguiente sin validar el anterior.
- No ampliar alcance (YAGNI): solo lo que pide la tarea.
- No tocar archivos fuera de la tarea. Si una decisión afecta spec/plan/contrato, proponer la edición del documento correspondiente.
- Indicar siempre qué archivos del paquete se usaron como contexto.

---

## 4. Definición de Hecho (DoD) — aplica a **toda** tarea

- [ ] Cumple todas las reglas C-xx relevantes.
- [ ] Cumple los criterios de aceptación de la spec (`Sxx-ACy`).
- [ ] 0 errores y 0 advertencias en la consola del navegador.
- [ ] Estados de carga / vacío / error / éxito presentes (si hay datos remotos).
- [ ] Probado con cada rol al que aplica y con datos inválidos.
- [ ] Sin `TODO` huérfanos (todo `TODO` referencia un `Q-xx` o un `Fx-Txx`).
- [ ] Documentos actualizados (spec, contrato, preguntas) + entrada en historial.

---

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Versión inicial |
