# Ejercicios — 29 DevOps IaC

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Dependency graph**
Añade a `TerraformResource` un campo `dependsOn: List<String>`. Implementa el ordenamiento topológico de recursos para que el apply se realice en el orden correcto. Si existe un ciclo, lanza una excepción `CircularDependencyException`.

---

**Ejercicio 2 — Terraform modules**
Modela `TerraformModule(name, resources, inputVars, outputVars)`. Permite componer módulos donde el output de uno puede ser input de otro. Resuelve el grafo de módulos y ejecuta el apply en el orden correcto.

---

**Ejercicio 3 — Drift detection**
Simula un estado "real en la nube" que puede diferir del state file. Compara ambos y detecta drift: reporta qué recursos fueron modificados externamente. Implementa la remediación con `terraform apply`.

---

**Ejercicio 4 — Ansible roles**
Modela `Role(name, tasks: List<AnsibleTask>)`. Un `Playbook` tiene una lista de roles por host. Cada host pertenece a uno o más grupos. Resuelve qué tareas se ejecutan en cada host según su grupo.

---

**Ejercicio 5 — Ansible idempotency**
Crea `IdempotentTask` que registra si ya fue ejecutada con éxito. Una segunda ejecución devuelve `SKIPPED` en lugar de `CHANGED`. Verifica el comportamiento con 10 tareas donde 5 ya fueron aplicadas anteriormente.
