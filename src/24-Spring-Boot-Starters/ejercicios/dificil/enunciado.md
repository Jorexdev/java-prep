# Ejercicios — 24 Spring Boot: Starters
## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Topological auto-config**

Define 5 auto-configs con dependencias: A depende de B, B depende de C,
D depende de B, E depende de C.
Implementa el algoritmo de ordenamiento topológico (Kahn's algorithm o DFS).
Ejecuta las auto-configs en el orden resuelto e imprime el orden real.
Demo mostrando los prints de cada auto-config con el número de orden.

---

**Ejercicio 2 — Condition override (exclude)**

Implementa `ExcludeAutoConfiguration(List<String> excluded)`.
Las auto-configs en la lista de exclusión NO se ejecutan aunque sus condiciones
se cumplan. Simula la anotación `@SpringBootApplication(exclude={...})`.
Demo con 5 auto-configs donde se excluyen 2 explícitamente.
Mostrar en el reporte que las excluidas son "Excluded by user".

---

**Ejercicio 3 — Typed starter properties**

El starter `"observability"` expone `ObservabilityProperties(enabled, samplingRate, endpoint)`
poblada desde el `Map<String, String>` de configuración.
Los beans del starter (`MetricsCollector`, `TracingFilter`) leen la instancia
de `ObservabilityProperties` para configurarse.
Implementa el ciclo completo: properties → beans. Demo con 3 configuraciones distintas.

---

**Ejercicio 4 — Test auto-config subset**

Implementa `TestContextFactory(List<String> allowedAutoConfigs)` que acepta
solo las auto-configs en la lista (simula `@ImportAutoConfiguration`).
Tiene disponibles 8 auto-configs, pero en los tests solo activa las 3 permitidas.
Demo comparando el contexto completo vs el contexto de test con subset de 3.
