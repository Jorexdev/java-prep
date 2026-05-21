# Ejercicios — 33 Herramientas-Gradle
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Task graph**
Implementa `Task(name, dependsOn, Runnable action)`.
Ordena las tareas topológicamente y ejecútalas en ese orden.
Si una tarea lanza una excepción, las tareas que dependen de ella no se ejecutan.
Demo con 6 tareas con dependencias entre sí. Muestra el orden de ejecución.

---

**Ejercicio 2 — UP-TO-DATE**
Añade a `Task` los campos `inputsHash` y `outputsHash`.
Si ambos coinciden con los valores del último build (guardados en un `Map` de snapshots),
la tarea está UP-TO-DATE y se saltea.
Demo: primera ejecución completa, segunda ejecución con todos los inputs iguales (UP-TO-DATE).

---

**Ejercicio 3 — Incremental task**
Implementa `IncrementalCompiler` que mantiene `Map<String,String> checksums` de archivos fuente.
Solo recompila los archivos cuyos checksums han cambiado desde la última ejecución.
Demo: 5 archivos fuente, cambiar 2 de ellos, mostrar que solo esos 2 se recompilan.

---

**Ejercicio 4 — Dependency configurations**
Implementa las configuraciones de Gradle: `api` (transitiva), `implementation` (no transitiva),
`testImplementation` (solo en test).
Resuelve el classpath de compile, runtime y testRuntime para un proyecto con 6 dependencias.

---

**Ejercicio 5 — Gradle Wrapper**
Implementa `GradleWrapper(distributionUrl, version)`.
Si la versión solicitada no está en caché (`Map<String, byte[]>`), "descárgala" (insertar en cache).
Si ya está, úsala directamente.
Demo con 3 versiones distintas de Gradle y accesos repetidos.

---

**Ejercicio 6 — Project properties**
Implementa la resolución de propiedades en orden de precedencia:
CLI `-P` (mayor) > variables de entorno > `gradle.properties` > valores por defecto (menor).
Demo con la misma clave definida en todas las fuentes. Muestra qué valor gana en cada caso.
