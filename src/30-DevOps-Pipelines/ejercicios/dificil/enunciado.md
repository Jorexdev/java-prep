# Ejercicios — 30 DevOps Pipelines

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Blue-green deployment**
Crea `Router` que dirige el tráfico a un entorno activo (blue o green). `BlueGreenDeployer` despliega a green, realiza el health check, cambia el router si es exitoso y deja blue idle. Si el health check falla, cancela sin cambiar el router. Implementa `rollback()` que revierte el switch del router. Demo con fallo en el health check que cancela el deploy.

---

**Ejercicio 2 — Canary release**
Enruta un porcentaje X del tráfico simulado (lista de requests con IDs) a la nueva versión. Si el error rate en canary supera el 5%, activa rollback automático. Incrementa progresivamente: 5%→25%→100%. Demo con error rate excesivo al llegar al 25% que activa el rollback y deja el 100% en la versión estable.

---

**Ejercicio 3 — GitOps reconciliation**
Crea `GitOpsController` con "estado deseado en git" (`Map<String,String>` recurso→spec) y "estado desplegado" (`Map<String,String>`). `reconcile()` calcula el diff y aplica: `apply(resource, spec)` para add/update, `delete(resource)` para eliminaciones. Simula 3 ciclos con cambios distintos entre ellos.

---

**Ejercicio 4 — Pipeline DSL**
Implementa un builder fluido: `Pipeline.named("ci").stage("compile").then("test").parallel("lint","security").then("deploy").build()`. El grafo resultante debe ejecutar `compile` → `test` → (`lint` y `security` en paralelo) → `deploy`. Muestra el grafo de stages y ejecuta el pipeline con output.
