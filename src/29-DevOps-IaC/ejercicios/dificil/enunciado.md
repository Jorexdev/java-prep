# Ejercicios — 29 DevOps IaC

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Remote state**
Implementa `RemoteStateStore` como singleton (Map simulado). Múltiples `TerraformWorkspace` comparten el mismo state. Añade `lock()` y `unlock()` para evitar applies concurrentes. Demuestra el comportamiento con 2 workspaces intentando aplicar simultáneamente.

---

**Ejercicio 2 — Terraform plan diff**
Dado dos snapshots de configuración (v1 y v2), genera un diff detallado: recursos añadidos, recursos eliminados y atributos cambiados por recurso. El output debe ser legible, similar al formato real de `terraform plan`.

---

**Ejercicio 3 — Import existing resource**
Simula `terraform import`: dado un recurso que ya existe en la "nube" (Map simulado), añádelo al state file sin recrearlo. Reconcilia el recurso importado con la configuración deseada y reporta cualquier diferencia.

---

**Ejercicio 4 — Ansible error handling**
Simula la construcción `block/rescue/always` de Ansible: si una tarea dentro del `block` falla, se ejecutan las tareas de `rescue`. Las tareas en `always` se ejecutan siempre, independientemente del resultado. Demuestra un escenario con fallo y recuperación exitosa.
