# Ejercicios — 33 Herramientas-Gradle
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Custom task**
Implementa una clase abstracta `GenerateCodeTask`.
Lee una lista de nombres de clases y genera un `String` Java por cada una: `"public class X { }"`.
Regístrala en una fase `codegen` del grafo de tareas.
Ejecuta y muestra el código generado por cada clase.

---

**Ejercicio 2 — Build variants**
Implementa `ProductFlavor(name)` y `BuildType(name)` (debug/release).
Genera todas las combinaciones como variantes: `flavorA-debug`, `flavorA-release`, etc.
Cada variante tiene su propio conjunto de dependencias.
Demo con 2 flavors × 2 build types = 4 variantes.

---

**Ejercicio 3 — Dependency substitution**
Implementa `DependencySubstitution` que reemplaza `"com.example:lib:1.0"` por `project(":lib")`
durante la resolución del grafo de dependencias.
Demo: un proyecto declara la dependencia externa, la sustitución la redirige al módulo local,
y se muestra el grafo de deps antes y después.

---

**Ejercicio 4 — Composite build**
Implementa `CompositeBuild` con 2 "included builds".
Al resolver una dependencia, busca primero en los included builds antes que en repositorios remotos.
Demo con una dependencia que existe en un included build y otra que solo está en el remoto.

---

**Ejercicio 5 — Configuration cache**
Implementa `ConfigCache` que guarda el task graph serializado (como `Map`).
En el segundo build, si los inputs de configuración no han cambiado, reutiliza el grafo cacheado.
Demo midiendo el tiempo: primer build (configura), segundo build (cache hit).

---

**Ejercicio 6 — Task graph con UP-TO-DATE**
Crea `GradleTask(name, inputs: Set<String>, outputs: Set<String>, List<String> dependsOn)`. Implementa `TaskRunner` que antes de ejecutar cada tarea comprueba si sus inputs y outputs no han cambiado desde la última ejecución (UP-TO-DATE check basado en hash de inputs). Si no hay cambios, la tarea se salta con estado `UP-TO-DATE`. El runner ejecuta las tareas en orden topológico. Demo con 5 tareas donde en el segundo build, 3 tareas están UP-TO-DATE y solo 2 se reejecutam porque sus inputs cambiaron.

---
