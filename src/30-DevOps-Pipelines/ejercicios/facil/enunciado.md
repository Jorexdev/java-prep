# Ejercicios — 30 DevOps Pipelines

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Pipeline stages**
Crea `Stage(name, Runnable task)`. Ejecuta los stages en secuencia. Si uno lanza una excepción, detén el pipeline y márcalo como `FAILED`. Muestra el resultado de cada stage (PASSED/FAILED) y el estado final del pipeline.

---

**Ejercicio 2 — Artifact**
Crea `Artifact(name, version, checksum)`. El stage "build" produce el artifact y calcula su checksum (suma de caracteres del nombre+versión, convertida a hex simulado). El stage "deploy" verifica que el checksum coincide antes de desplegar. Si no coincide, aborta el despliegue. Demo con un artifact válido y uno con checksum alterado.

---

**Ejercicio 3 — Environment promotion**
Crea `Environment(name)` con lista de artifacts desplegados. Implementa la promoción: un artifact puede pasar de `dev` a `staging` solo si los tests pasaron (`boolean testsPassed`). De `staging` a `prod` solo si hay aprobación manual (`boolean approved`). Demo con un artifact que pasa todo el flujo y otro que se queda bloqueado en staging.

---

**Ejercicio 4 — Rollback**
Crea `DeploymentHistory` usando un `Deque<DeployRecord>` donde `DeployRecord(version, timestamp, success)`. Implementa `rollback()` que vuelve al último deploy exitoso, ignorando los fallidos. Demo con un historial de 5 versiones donde las 2 últimas fallaron.

---

**Ejercicio 5 — Pipeline variables**
Crea `PipelineContext(Map<String,String>)` con métodos `get(key)` y `set(key, value)`. Los stages leen y escriben variables del contexto (`BUILD_NUMBER`, `TEST_RESULT`, `ARTIFACT_PATH`, `DEPLOY_ENV`). Demo con una cadena de 4 stages que comparten y modifican el contexto.

---

**Ejercicio 6 — Stage dependencies**
Crea `Stage(name, dependsOn)`. Ordena los stages topológicamente. Los stages sin dependencias se ejecutan en paralelo con `Thread`. Muestra el orden real de inicio de cada stage (pueden solaparse en el tiempo).
