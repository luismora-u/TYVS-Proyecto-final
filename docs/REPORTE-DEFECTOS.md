# Reporte de Defectos y Gestión de Calidad — Proyecto Final

**Asignatura:** Testing y Validación de Software — Maestría en Ingeniería de Software, U. de La Sabana
**Proyecto:** API Registraduría (Spring Boot, arquitectura hexagonal)
**Fecha:** 16 de junio de 2026

---

## 1. Introducción

Aplica el **ciclo de vida del defecto** a los defectos reales del proyecto final, detectados
a lo largo de **todas las fases de prueba** (unitaria, integración, sistema y rendimiento).
Incluye los defectos funcionales del dominio —hoy cubiertos por la suite de **34 pruebas**
como regresión— y los hallazgos de **rendimiento** descubiertos en las pruebas de carga
(Paso 5), entre ellos el cuello de botella de `GET /voters`.

Datos tabulares en [`data/defectos.csv`](../data/defectos.csv); visualización en
[`dashboard/dashboard.html`](../dashboard/dashboard.html).

## 2. Defectos priorizados

| ID | Título | Tipo | Severidad | Prioridad | Fase | Estado |
|----|--------|------|-----------|-----------|------|--------|
| **DEF-03** | Doble inscripción del mismo documento | Funcional | **Crítico** | P1 | Integración | Cerrado |
| **DEF-01** | `GET /voters` se degrada bajo carga (sin paginación) | Rendimiento/Diseño | Mayor | P1 | Rendimiento | **Abierto** |
| **DEF-04** | REST devuelve 500 ante género inválido | Funcional | Mayor | P1 | Sistema | Cerrado |
| **DEF-05** | Menor de edad queda persistido | Funcional | Mayor | P1 | Integración | Cerrado |
| **DEF-06** | Excepción de BD se propaga cruda | Técnico | Menor | P2 | Integración | Cerrado |
| **DEF-02** | p99 elevado por cold-start de la JVM | Rendimiento | Menor | P3 | Rendimiento | **Abierto** |

## 3. Ciclo de vida detallado (defectos del proyecto final)

### DEF-01 — `GET /voters` se degrada bajo carga por falta de paginación `[Mayor · P1 · Abierto]`

- **Identificación:** en el escenario de pico (100 usuarios) la lectura `GET /voters` alcanzó
  un **p95 de 486 ms**, frente a 137 ms de `POST /register`. Detectado en la fase de
  **rendimiento** (JMeter, Paso 5).
- **Clasificación:** Rendimiento / diseño. Severidad **Mayor** (incumple el SLO de lectura bajo
  carga). Prioridad **P1** (es la mejora principal del proyecto).
- **Seguimiento y validación:**
  - *Causa raíz:* `Registry.listVoters()` ejecuta `findAll()` sobre una tabla que **crece**
    durante la prueba y el endpoint **serializa la lista completa** sin límite ni paginación.
    A mayor volumen acumulado, cada respuesta es más costosa → degradación no lineal del p95.
  - *Acción propuesta (pendiente):* paginar `GET /voters` (`?page`, `?size`) y aplicar `LIMIT`
    en la consulta; opcionalmente índices y base de datos real.
- **Cierre:** **no cerrado.** Permanece **Abierto** en el backlog como mejora prioritaria.
  Documentado en [`docs/REPORTE-RENDIMIENTO.md`](REPORTE-RENDIMIENTO.md) (§4).

### DEF-02 — Percentil 99 elevado por arranque en frío de la JVM `[Menor · P3 · Abierto]`

- **Identificación:** máximos de 1.3–1.5 s en las primeras peticiones de smoke y carga
  (la mediana se mantenía en milisegundos). Fase de **rendimiento**.
- **Clasificación:** Rendimiento (JIT/GC/arranque). Severidad **Menor**. Prioridad **P3**.
- **Seguimiento y validación:** *causa raíz:* sin fase de calentamiento previa a la medición.
  *Acción propuesta:* warm-up de la JVM o descartar el primer tramo del ramp-up.
- **Cierre:** **Abierto** (backlog).

### DEF-03 — Doble inscripción del mismo documento `[Crítico · P1 · Cerrado]`

- **Identificación:** dos registros con el mismo `id` se aceptaban como `VALID`. Fase de
  **integración**.
- **Clasificación:** Funcional — regla de negocio (RN-02). Severidad **Crítico**. Prioridad **P1**.
- **Seguimiento y validación:** *causa raíz:* faltaba `existsById` antes de `save`.
  *Corrección:* verificación previa en `registerVoter`. *Verifica:*
  `shouldRejectDuplicatedDocument` + `shouldReturnDuplicatedWhenRepoSaysExists`.
- **Cierre:** **Cerrado** — pruebas en verde y conservadas como regresión.

### DEF-04 — REST devuelve 500 ante género inválido `[Mayor · P1 · Cerrado]`

- **Identificación:** `POST /register` con género no válido devolvía 500. Fase de **sistema**.
- **Clasificación:** Funcional — manejo de errores (RF-API). Severidad **Mayor**. Prioridad **P1**.
- **Seguimiento y validación:** *causa raíz:* `Gender.valueOf` sin manejo. *Corrección:*
  `ApiExceptionHandler` + `parseGender` → 400. *Verifica:*
  `shouldReturnBadRequestWhenGenderIsInvalid`. Confirmado en carga: **0 respuestas 500**.
- **Cierre:** **Cerrado**.

### DEF-05 — Menor de edad queda persistido `[Mayor · P1 · Cerrado]`

- **Identificación:** una persona de 16 años quedaba insertada. Fase de **integración**.
- **Clasificación:** Funcional — integridad de datos (RN-04). Severidad **Mayor**. Prioridad **P1**.
- **Seguimiento y validación:** *causa raíz:* validación de edad tras persistir. *Corrección:*
  regla `age<18` antes de tocar el repositorio. *Verifica:* `shouldNotPersistUnderagePerson`.
- **Cierre:** **Cerrado**.

### DEF-06 — Excepción de BD se propaga cruda `[Menor · P2 · Cerrado]`

- **Identificación:** una excepción del repositorio se propagaba sin controlar. Fase de
  **integración (mock)**.
- **Clasificación:** Técnico — robustez. Severidad **Menor**. Prioridad **P2**.
- **Seguimiento y validación:** *corrección:* `try/catch` → `IllegalStateException`.
  *Verifica:* `shouldWrapRepositoryFailure`.
- **Cierre:** **Cerrado**.

## 4. Matriz de trazabilidad

| Defecto | Requisito | Prueba / Detección | Componente | Solución | Estado |
|---------|-----------|--------------------|------------|----------|--------|
| DEF-01 | RNF-Rendimiento (p95 < 200 ms) | JMeter escenario pico | `Registry.listVoters` / REST | paginación (propuesta) | Abierto |
| DEF-02 | RNF-Rendimiento (latencia estable) | JMeter smoke/carga | runtime JVM | warm-up (propuesta) | Abierto |
| DEF-03 | RN-02 | `shouldRejectDuplicatedDocument` | `Registry` | `existsById` previo | Cerrado |
| DEF-04 | RF-API | `shouldReturnBadRequestWhenGenderIsInvalid` | `RegistryController` | `ApiExceptionHandler` | Cerrado |
| DEF-05 | RN-04 | `shouldNotPersistUnderagePerson` | `Registry` | regla edad previa | Cerrado |
| DEF-06 | RNF-Robustez | `shouldWrapRepositoryFailure` | `Registry` | `try/catch` | Cerrado |

## 5. Registro en GitHub Issues

Los defectos pueden registrarse como **GitHub Issues** con la plantilla
[`.github/ISSUE_TEMPLATE/defecto.md`](../.github/ISSUE_TEMPLATE/defecto.md), con etiquetas de
tipo, severidad y prioridad, y el estado reflejado en el ciclo de vida del issue.

## 6. Reflexión

El proyecto final muestra dos caras de la calidad: los defectos **funcionales** (DEF-03 a
DEF-06) están **cerrados y blindados** por la suite de 34 pruebas automatizadas (regresión),
con una **eficiencia de remoción del 100 %** (0 escaparon a producción). Los defectos de
**rendimiento** (DEF-01, DEF-02), descubiertos por las pruebas de carga, permanecen **abiertos**
como mejoras priorizadas: demuestran que las pruebas de rendimiento **detectan** problemas de
diseño que las pruebas funcionales no revelan. La trazabilidad requisito ↔ prueba ↔ solución
garantiza que cada cierre es verificable.
