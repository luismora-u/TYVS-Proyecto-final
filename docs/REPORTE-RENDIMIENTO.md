# Reporte de Pruebas de Rendimiento y Carga — Proyecto Final

**Herramienta:** Apache JMeter 5.6.3 (modo no-GUI)
**SUT:** API Registraduría (Spring Boot 2.7.18 + H2 en memoria) — `POST /register` y `GET /voters`
**Entorno:** Intel i5-7200U (4 hilos lógicos), 8 GB RAM, Windows 10, JDK 17 — generador y SUT co-ubicados
**Fecha:** 16 de junio de 2026

---

## 1. Objetivo

Evaluar el desempeño de la API ampliada bajo carga creciente, midiendo una **mezcla realista
de lectura y escritura**: cada usuario virtual registra un votante (`POST /register`) y luego
consulta el listado (`GET /voters`), con *think-time* de 200–500 ms.

## 2. Escenarios y parámetros

Un único plan parametrizable (`jmeter/proyecto-final-loadtest.jmx`) ejecutado 3 veces:

| Escenario | Hilos | Ramp-up | Duración | Propósito |
|-----------|------:|--------:|---------:|-----------|
| Smoke | 1 | 1 s | 30 s | Línea base |
| Carga | 50 | 10 s | 60 s | Concurrencia normal |
| Pico | 100 | 20 s | 60 s | Concurrencia alta |

Parámetros comunes: think-time 200–500 ms, keep-alive, aserciones (POST → 200/400; GET → 200),
timeouts 5 s/15 s, carga por duración.

## 3. Métricas obtenidas (reales)

### Totales por escenario

| Métrica | Smoke | Carga | Pico |
|---------|------:|------:|-----:|
| Muestras | 80 | 7 682 | 11 575 |
| **% Error** | **0.00 %** | **0.00 %** | **0.00 %** |
| Mediana (ms) | 8 | 4 | 22 |
| p95 (ms) | 16 | 19 | 382 |
| Máximo (ms) | 1 548 | 1 307 | 1 332 |
| Throughput (req/s) | 2.7 | 129 | 194 |

### Desglose por endpoint (escenario Pico, 100 usuarios)

| Endpoint | Muestras | Mediana | p95 | Máx | Throughput |
|----------|---------:|--------:|----:|----:|-----------:|
| `POST /register` (escritura) | 5 813 | 5 ms | 137 ms | 1 332 ms | 97.4/s |
| `GET /voters` (lectura) | 5 762 | 32 ms | **486 ms** | 1 206 ms | 99.1/s |

## 4. Interpretación

- **Robustez total:** **0 % de errores** y **0 respuestas HTTP 500** en ~19 000 peticiones.
  La aplicación respondió correctamente a toda la carga.
- **Estado estable excelente:** con 50 usuarios la mediana es de **4 ms** (p95 19 ms). La
  lógica de negocio + H2 en memoria es muy ligera.
- **Cold start de la JVM:** los máximos (~1.3–1.5 s) corresponden a las primeras peticiones
  (arranque en frío, JIT, GC), no al estado estable. La mediana lo confirma.
- **Cuello de botella identificado — `GET /voters` sin paginación:** bajo 100 usuarios, la
  lectura se degrada mucho más (p95 **486 ms**) que la escritura (p95 137 ms). La causa es de
  diseño: `GET /voters` ejecuta `findAll()` sobre una tabla que **crece continuamente** durante
  la prueba (cada `POST` inserta filas) y **serializa la lista completa** sin límite ni
  paginación. A mayor volumen acumulado, cada respuesta es más costosa (más filas leídas + JSON
  más grande), de ahí la degradación no lineal del p95.

## 5. Mejoras propuestas

1. **Paginación en `GET /voters`** (`?page`, `?size`) y/o `LIMIT` en la consulta — corrige el
   cuello de botella principal.
2. **Índices y base real** (PostgreSQL) para medir con persistencia de producción.
3. **Warm-up de la JVM** antes de medir, para que los percentiles altos no reflejen el arranque.
4. **Separar generador y SUT** para hallar el límite real de la aplicación.
5. **SLO explícito** (p. ej., p95 < 200 ms con 0 % de error hasta 100 usuarios) — hoy se cumple
   para la escritura pero **no** para la lectura, lo que prioriza la mejora 1.

## 6. Evidencia

Dashboards HTML por escenario en `results/<escenario>-report/index.html` y resultados crudos
en `results/<escenario>.jtl`. Reproducible con el comando documentado en el README (sección
"Pruebas de rendimiento").

## 7. Conclusión

La API es **robusta y rápida** en condiciones normales (0 % de error, mediana de milisegundos).
Las pruebas de carga cumplieron su doble propósito: **validar el desempeño** y **descubrir un
defecto de diseño** (lectura sin paginación) que se gestiona en el reporte de defectos. Esto
demuestra el valor de las pruebas de rendimiento como herramienta de detección temprana, no
solo de medición.
