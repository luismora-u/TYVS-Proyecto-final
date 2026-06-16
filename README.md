# TYVS — Proyecto Final Integrador (Registraduría)

Proyecto **final** de la asignatura *Testing y Validación de Software* (Maestría en Ingeniería
de Software, Universidad de La Sabana). Integra en un solo repositorio **todo lo trabajado en
el curso** aplicado a una API REST de la **Registraduría** (registro de votantes), construida
en **Java 17 + Spring Boot + Maven** con arquitectura hexagonal.

> **Estado:** 🚧 En construcción por pasos. Este README se completará en el Paso 7.
> **Autor (entrega individual):** ver [`integrantes.txt`](integrantes.txt).

---

## Alcance (qué integra)

| Disciplina del curso | Herramienta | Estado |
|----------------------|-------------|:------:|
| Pruebas unitarias (TDD) | JUnit + Mockito | ⏳ |
| Pruebas de integración | JUnit + H2 | ⏳ |
| Pruebas de sistema (REST) | MockMvc / TestRestTemplate | ⏳ |
| Cobertura de código | JaCoCo (≥ 80 %) | ⏳ |
| Pruebas de rendimiento y carga | Apache JMeter | ⏳ |
| Integración continua (CI/CD) | GitHub Actions | ⏳ |
| Gestión de defectos | GitHub Issues + dashboard | ⏳ |

## API objetivo (Registraduría ampliada)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/register` | Registrar votante (reglas de negocio) |
| GET | `/voters/{id}` | Consultar votante por documento |
| GET | `/voters` | Listar votantes registrados |
| DELETE | `/voters/{id}` | Eliminar un registro |
| GET | `/stats` | Estadísticas del padrón |

## Cómo ejecutar (base actual)

```bash
mvn clean verify          # compila + pruebas + cobertura
mvn spring-boot:run       # levanta la API en http://localhost:8080
```

---

## Créditos

Dominio Registraduría basado en el material del profesor **César Augusto Vega Fernández** —
Universidad de La Sabana. Proyecto final desarrollado por el estudiante.
