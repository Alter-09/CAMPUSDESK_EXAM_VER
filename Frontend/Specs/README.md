# CampusDesk Frontend · Paquete de Especificaciones (Spec Driven Development)

| Campo | Valor |
|---|---|
| Proyecto | CampusDesk (TechNova Solutions) — gestión de incidencias tecnológicas |
| Alcance | **Exclusivamente FrontEnd** (HTML + CSS + JavaScript vanilla) |
| Responsable | Sebbs (rol único: FrontEnd) |
| Versión del paquete | 0.1.0 (borrador — sin fases iniciadas) |
| Última actualización | 2026-10-08 |
| Fuentes | `CAMPUSDESK_SPRINGBOOT.md` (enunciado) · `CAMPUSDESK_BACKEND.md` (propuesta backend) · `DESIGN.md` "Titan Meridian" (sha256 `86a387eff03273ad…`) · especificaciones personales de Sebbs |

## 1. Qué es este paquete

Un conjunto de documentos Markdown **vivos**: cada fase puede añadir requisitos, controles de calidad o decisiones sin rehacer todo. Siguen el ciclo SDD:

```
Constitución (reglas) → Spec (qué) → Plan (cómo) → Contrato API → Fases/Tareas → Implementar → Auditar → Actualizar docs
```

## 2. Archivos y cuándo cargarlos (ahorro de tokens)

| Archivo | Rol | Cambia cuando… | Cargarlo para… |
|---|---|---|---|
| `00-constitution.md` | Reglas no negociables + protocolo **Cero Suposiciones** | Sebbs cambia una regla | **Siempre** (es corto) |
| `01-spec.md` | QUÉ se construye: roles, pantallas, validaciones, UX, errores | Cambian requisitos | Implementar o revisar una pantalla |
| `02-plan.md` | CÓMO: arquitectura, carpetas, SOLID, HTTP/mocks, Web Components | Cambian decisiones técnicas | Escribir infraestructura o componentes |
| `03-api-contract.md` | Contrato con el backend (provisional hasta el Swagger) | Llega/cambia el Swagger | Escribir servicios, mocks o mappers |
| `04-phases.md` | Fases, tareas, puertas de calidad, auditorías, bitácoras | Avanza el trabajo | Planear o auditar la fase actual |
| `05-questions-and-decisions.md` | Preguntas abiertas (Q-xx) y decisiones (D-xx) | Cada duda/respuesta | Antes de empezar cualquier tarea |
| `06-design-system.md` | `DESIGN.md` traducido a tokens CSS, tipografía, componentes y hallazgos | Cambia `DESIGN.md` o Figma | Escribir CSS o componentes visuales |

**Regla práctica:** carga `00` + el archivo del tipo de tarea + solo la sección de la fase actual de `04`.

## 3. Leyenda de estado (se usa en todos los archivos)

| Marca | Significado |
|---|---|
| ✅ | Confirmado (lo dijo Sebbs, el enunciado o el backend) |
| 🔶 | **Propuesta del spec — NO confirmada.** No se implementa hasta que pase a ✅ |
| ❓ | Pendiente: depende de una pregunta `Q-xx` |
| 🔒 | Bloqueado por una dependencia externa (backend, Figma) |
| ⬜ 🟦 🟨 ✔️ | Fase: no iniciada · en curso · en auditoría · cerrada |

## 4. Cómo mantener estos documentos vivos

1. **IDs inmutables** (`C-`, `S-`, `Q-`, `D-`, `F`, `AUD-`): nunca se reutilizan. Lo obsoleto se marca `🗑️ Obsoleto (motivo, fecha)`, no se borra.
2. Toda modificación suma una fila al **Historial de cambios** del archivo y sube su versión.
3. Una respuesta a una `Q-xx` se registra primero en `05`, y luego se propaga a spec/plan/contrato.
4. Al cerrar una fase: actualizar tracker (`04`), bitácoras, y subir la versión del paquete.
5. Si `DESIGN.md` cambia (su hash difiere), revisar `06` antes de tocar CSS.

## 5. Orden recomendado de arranque

1. Leer `00` §3 (Protocolo Cero Suposiciones).
2. Responder las preguntas 🔴 de la **Fase 0** en `05`.
3. Entregar los insumos de Figma indicados en `02` §12 y `06` §9.
4. Cerrar la Fase 0 (`04`) y recién ahí empezar a escribir código.

## Historial de cambios

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1.0 | 2026-10-08 | Creación del paquete (7 archivos + README), incluyendo la traducción de `DESIGN.md` y sus hallazgos de contraste |
