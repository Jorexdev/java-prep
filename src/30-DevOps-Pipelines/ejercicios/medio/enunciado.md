# Ejercicios — 30 DevOps Pipelines

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Matrix build**
Define las dimensiones `[java21, java17]` × `[linux, windows]` = 4 combinaciones. Ejecuta todas en paralelo con `Thread`. Cada combinación simula una build con un tiempo de espera corto aleatorio. Muestra una tabla de resultados al final con el estado de cada combinación.

---

**Ejercicio 2 — Parallel stages**
Crea `ParallelGroup(name, List<Stage>)`. Ejecuta todos los stages con `Thread`. El grupo termina cuando todos completan. Si alguno falla (lanza excepción), el grupo completo se marca como fallido. Demo con 4 stages paralelos donde el tercero falla.

---

**Ejercicio 3 — Secrets management**
Crea `SecretStore` con valores almacenados con ROT13. Los stages acceden a los secrets por nombre, el store devuelve el valor desencriptado. En los logs, los valores de secrets deben aparecer enmascarados como `***`. Demo con 3 secrets (DB_PASS, API_KEY, JWT_SECRET) usados en distintos stages.

---

**Ejercicio 4 — Conditional execution**
Crea `Stage` con un campo `Predicate<PipelineContext> when`. El stage solo se ejecuta si `when` es null o evalúa a `true`. Demo con 4 stages donde `deploy-staging` requiere `branch != main` y `deploy-prod` requiere `branch == main && tests == passed`.

---

**Ejercicio 5 — Test report**
Crea `TestResult(suite, passed, failed, skipped)`. Agrega los resultados de 4 suites de tests. Genera un reporte final con los totales (total passed, failed, skipped), el porcentaje de éxito y la lista detallada de failures (suites con failed > 0).
