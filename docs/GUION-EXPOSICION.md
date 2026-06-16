# Guion de Exposición — Proyecto Final (Registraduría)

Guion sugerido para una presentación de ~10–12 minutos con demo en vivo. La idea es mostrar
**cómo un mismo proyecto recorre todas las disciplinas de Testing y Validación**.

---

## 0. Antes de empezar (preparación)
- Abrir una terminal **PowerShell** en la carpeta del proyecto.
- Tener listo el comando: `pwsh ./demo.ps1` (o ejecutarlo antes y dejar los reportes abiertos).
- Tener el repo abierto en GitHub (pestañas **Actions** e **Issues**).

---

## 1. Presentación del proyecto (1 min)
- "Es una API REST de la **Registraduría** en Java 17 + Spring Boot con **arquitectura
  hexagonal**: el dominio no depende de la infraestructura, lo que hace el código **testeable**."
- Mostrar el diagrama de capas del README y los 5 endpoints.

## 2. Pruebas unitarias y TDD (2 min)
- "Apliqué **TDD**: primero la prueba (Red), luego el código (Green)."
- Mostrar `git log --oneline` → el commit **"Paso 2 (TDD)"** evidencia el ciclo.
- Abrir `RegistryQueryUnitTest` y explicar 1–2 casos (p. ej. `shouldComputeStatsFromRegisteredVoters`).
- Mensaje clave: las unitarias validan **reglas de negocio** aisladas y rápidas (Mockito).

## 3. Integración y sistema (1.5 min)
- "La **pirámide de pruebas**: 12 unitarias, 8 de integración, 10 de sistema = **34**."
- Integración (H2): valida la colaboración real servicio ↔ base de datos.
- Sistema (REST): `VotersControllerSystemTest` verifica códigos HTTP reales (200, **404**, **204**).

## 4. Cobertura + CI/CD (1.5 min)
- Ejecutar (o mostrar ya ejecutado) `mvn clean verify` → **"All coverage checks have been met"**.
- Abrir el reporte JaCoCo: **91.5 %** de líneas (umbral 80 %).
- Mostrar en GitHub la pestaña **Actions** con el pipeline **verde**: "cada push corre las 34
  pruebas y la cobertura en la nube; con branch protection, **bloquea merges** si algo falla."

## 5. Rendimiento y carga (2 min)
- "Con JMeter simulé usuarios concurrentes sobre lectura y escritura, con *think-time*."
- Abrir el dashboard `results/02-carga-report/index.html`: **0 % de error**, mediana ~4 ms.
- **Hallazgo estrella:** en el pico (100 usuarios), `GET /voters` se degrada (p95 **486 ms**)
  porque hace `findAll()` sin **paginación** sobre una tabla que crece. "La prueba de carga
  **descubrió un defecto de diseño**, no solo midió."

## 6. Gestión de defectos (1.5 min)
- Abrir `dashboard/dashboard.html`: 6 defectos, **DRE 100 %**, 4 cerrados / 2 abiertos.
- Mostrar la pestaña **Issues** de GitHub con los defectos registrados y etiquetados.
- "Los funcionales están **cerrados y blindados** por las pruebas (regresión); los de
  rendimiento quedan **abiertos** en backlog con su trazabilidad."

## 7. Cierre (1 min)
- Ejecutar `pwsh ./demo.ps1 -SkipJMeter` en vivo (o mostrarlo corrido): "un solo comando
  compila, prueba, mide cobertura, levanta la API y abre los reportes — **ejecutable y
  autónomo**."
- Frase final: "El proyecto demuestra que la calidad no es una fase, sino un **proceso
  continuo y trazable**: desde la regla de negocio probada con TDD hasta el defecto de
  rendimiento gestionado con métricas."

---

## Posibles preguntas
- **¿Por qué JUnit 4 y no 5?** Continuidad con el material del curso; se ejecuta sobre JUnit
  Platform vía *vintage engine*.
- **¿Por qué H2 en memoria?** Permite pruebas de integración reales y desechables; en producción
  se cambiaría por PostgreSQL (es justo una de las mejoras propuestas).
- **¿El error bajo carga es de la app?** No: en este proyecto fue un **defecto de diseño**
  (lectura sin paginación). En la Actividad 5 previa, en cambio, fue un límite del **cliente**
  (puertos efímeros de Windows). Saber distinguirlos es parte del aprendizaje.
