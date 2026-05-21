# Ejercicios — 29 DevOps IaC

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Resource model**
Crea la clase `TerraformResource(type, name, Map<String,String> config)`. Implementa un método `plan(desired)` que compara el estado deseado con el actual y lista las acciones: ADD, CHANGE o DESTROY. Demuestra el plan con 3 recursos donde uno es nuevo, uno tiene cambios y uno debe destruirse.

---

**Ejercicio 2 — State file**
Crea `TerraformState` con un `Map<String, TerraformResource>` como estado actual. Implementa `apply(desired)` que actualiza el state con los recursos deseados. Muestra el state antes y después del apply.

---

**Ejercicio 3 — Idempotency**
Extiende el ejercicio anterior. Ejecuta `apply()` dos veces seguidas con la misma configuración. Verifica que la segunda ejecución no produce cambios: `plan()` debe devolver una lista vacía.

---

**Ejercicio 4 — Variables**
Crea `TerraformConfig` con un `Map<String, String> variables`. Los valores de configuración pueden contener referencias del tipo `${var.nombre}`. Implementa la resolución de variables para que se sustituyan por sus valores reales.

---

**Ejercicio 5 — Ansible task**
Modela `AnsibleTask(name, module, params, when)`. La tarea solo se ejecuta si `when` es null o evalúa a true. Registra el resultado de cada tarea como uno de los estados: `CHANGED`, `OK` o `FAILED`.

---

**Ejercicio 6 — Ansible playbook**
Crea un playbook como lista de `AnsibleTask`. Ejecuta las tareas en orden y detente si una falla. Al finalizar muestra el resumen: cuántas resultaron en CHANGED, OK y FAILED.
