# Ejercicios — 33 Herramientas-Gradle
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Plugin development**
Implementa `GradlePlugin` con `apply(Project)`.
El plugin debe:
- Registrar una tarea `hello` que imprime `"Hello from plugin!"`
- Añadir la dependencia `junit:junit:4.13` a la configuración `testImplementation`
- Configurar una extensión `GreetingExtension(message)` accesible desde el proyecto

Demo: aplica el plugin a un proyecto simulado y muestra el estado del proyecto antes y después.

---

**Ejercicio 2 — Build scan**
Recopila durante el build las siguientes métricas:
- Duración de cada tarea (en ms)
- Cache hits y misses
- Resultados de tests (passed/failed)
- Número de dependencias

Genera un reporte de texto al finalizar el build.
Demo con un build de 8 tareas.

---

**Ejercicio 3 — Parallel execution**
Simula `--parallel`: ejecuta con threads las tareas que no tienen dependencias entre sí.
Demo con 8 tareas donde 4 son independientes y se ejecutan en paralelo.
Muestra la ganancia de tiempo comparando ejecución secuencial vs paralela.

---

**Ejercicio 4 — Artifact transforms**
Implementa `ArtifactTransform(from, to, Function<byte[],byte[]> transformer)`.
El sistema aplica automáticamente los transforms necesarios cuando el consumer pide un tipo
distinto al que produce el producer.
Demo con chain: `jar -> classes -> filtered-classes`.
Muestra qué transforms se aplican y en qué orden.

---

**Ejercicio 5 — Build cache: hash de inputs/outputs**
Implementa `BuildCache` que almacena resultados de tareas indexados por el hash de sus inputs (`Map<String, CacheEntry>`). `CacheEntry` contiene el hash de inputs, los outputs y el timestamp. Al ejecutar una tarea: si el hash de los inputs actuales coincide con una entrada en caché, es `CACHE HIT` y se reutilizan los outputs; si no, es `CACHE MISS` y se ejecuta la tarea guardando los nuevos outputs. Demo con 6 tareas, primer build completo (todos MISS), luego cambiar los inputs de 2 tareas y hacer segundo build (4 HITS, 2 MISS). Muestra el ahorro de tiempo acumulado.

---
