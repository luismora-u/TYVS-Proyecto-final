# TYVS — Proyecto Final Integrador (Registraduría)

Proyecto **final** de *Testing y Validación de Software* (Maestría en Ingeniería de Software,
Universidad de La Sabana). Integra en un solo repositorio **todas las disciplinas del curso**
aplicadas a una API REST de la **Registraduría** (registro de votantes), en
**Java 17 + Spring Boot + Maven** con arquitectura hexagonal.

> **Autor (entrega individual):** ver [`integrantes.txt`](integrantes.txt).

[![CI/CD](https://github.com/luismora-u/TYVS-Proyecto-final/actions/workflows/ci.yml/badge.svg)](https://github.com/luismora-u/TYVS-Proyecto-final/actions/workflows/ci.yml)

---

## 1. Qué integra (todo el curso en un proyecto)

| Disciplina | Herramienta | Evidencia |
|------------|-------------|-----------|
| Pruebas unitarias (TDD) | JUnit 4 + Mockito | `src/test/.../unit` |
| Pruebas de integración | JUnit + H2 | `src/test/.../integration` |
| Pruebas de sistema (REST) | TestRestTemplate | `src/test/.../system` |
| Cobertura de código (≥ 80 %) | JaCoCo | **91.5 %** — `target/site/jacoco` |
| Pruebas de rendimiento y carga | Apache JMeter | `jmeter/`, `results/`, [reporte](docs/REPORTE-RENDIMIENTO.md) |
| Integración continua (CI/CD) | GitHub Actions | [`.github/workflows/ci.yml`](.github/workflows/ci.yml) |
| Gestión de defectos | GitHub Issues + dashboard | [reporte](docs/REPORTE-DEFECTOS.md), [dashboard](dashboard/dashboard.html) |

**Estado de calidad:** 34 pruebas verdes · 91.5 % cobertura · 0 errores HTTP 500 en ~19 000
peticiones de carga · DRE 100 %.

## 2. La API (Registraduría)

| Método | Endpoint | Descripción | Respuestas |
|--------|----------|-------------|-----------|
| GET | `/` | Índice de la API (servicio + endpoints) | 200 |
| POST | `/register` | Registrar votante (reglas de negocio) | 200 (texto) / 400 |
| GET | `/voters/{id}` | Consultar votante | 200 / 404 |
| GET | `/voters` | Listar votantes | 200 |
| DELETE | `/voters/{id}` | Eliminar votante | 204 / 404 |
| GET | `/stats` | Estadísticas del padrón | 200 |

**Reglas de negocio** (`Registry.registerVoter`): `null`/`id<=0` → INVALID · `!alive` → DEAD ·
`age<18` → UNDERAGE · documento repetido → DUPLICATED · si cumple → VALID.

## 3. Ejecución autónoma (para la demo / exposición)

Un solo comando ejecuta **todo** el ciclo (pruebas + cobertura + app + JMeter + reportes):

```powershell
pwsh ./demo.ps1
# Variantes:
pwsh ./demo.ps1 -SkipJMeter   # sin pruebas de carga (más rápido)
pwsh ./demo.ps1 -SkipTests    # solo levantar la app y abrir dashboards
```

> El script detecta Maven/JMeter en el `PATH` o en `C:\tools\...`.

## 4. Ejecución manual

```bash
mvn clean verify          # compila + 34 pruebas + cobertura (umbral 80%)
mvn spring-boot:run       # API en http://localhost:8080

# Pruebas de rendimiento (con la app arriba)
jmeter -n -t jmeter/proyecto-final-loadtest.jmx -l results/carga.jtl -e -o results/carga-report \
       -Jthreads=50 -Jrampup=10 -Jduration=60
```

Ejemplos:

```bash
curl -X POST localhost:8080/register -H "Content-Type: application/json" \
     -d '{"name":"Ana","id":100,"age":30,"gender":"FEMALE","alive":true}'   # -> VALID
curl localhost:8080/voters/100      # -> {"id":100,"name":"Ana","age":30,"alive":true}
curl localhost:8080/stats           # -> {"total":1,"averageAge":30.0,"minAge":30,"maxAge":30}
```

## 5. Estructura

```
TYVS-Proyecto-final/
├── src/main/java/...         # API (dominio, aplicación, infraestructura, REST)
├── src/test/java/...         # 34 pruebas (unit / integration / system)
├── jmeter/                   # plan .jmx de rendimiento
├── results/                  # .jtl + dashboards HTML de JMeter
├── dashboard/dashboard.html  # dashboard de métricas de calidad (defectos)
├── data/defectos.csv         # registro de defectos
├── docs/
│   ├── REPORTE-RENDIMIENTO.md
│   ├── REPORTE-DEFECTOS.md
│   └── GUION-EXPOSICION.md   # guion de la presentación
├── .github/workflows/ci.yml  # pipeline CI/CD
├── demo.ps1                  # ejecución autónoma
└── README.md
```

## 6. Documentación

- **Rendimiento:** [`docs/REPORTE-RENDIMIENTO.md`](docs/REPORTE-RENDIMIENTO.md)
- **Defectos y calidad:** [`docs/REPORTE-DEFECTOS.md`](docs/REPORTE-DEFECTOS.md) · [dashboard](dashboard/dashboard.html)
- **Guion de exposición:** [`docs/GUION-EXPOSICION.md`](docs/GUION-EXPOSICION.md)

---

## Créditos

Dominio Registraduría basado en el material del profesor **César Augusto Vega Fernández** —
Universidad de La Sabana. Proyecto final desarrollado por el estudiante.
