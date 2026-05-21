# Ejercicios — 32 Herramientas-Maven
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Lifecycle phases**
Implementa `MavenLifecycle` con las fases en orden: `validate, compile, test, package, verify, install, deploy`.
`run("package")` ejecuta todas las fases hasta `package` inclusive, en orden.
Asocia un `Runnable` a cada fase para simular su ejecución.
Demo mostrando las fases ejecutadas y su output.

---

**Ejercicio 2 — Dependency model**
Implementa `Dependency(groupId, artifactId, version, scope)`.
Crea un POM simulado con 8 dependencias en distintos scopes (`compile`, `test`, `runtime`, `provided`).
Muestra las dependencias agrupadas por scope.

---

**Ejercicio 3 — Transitive dependencies**
A depende de B:1.0 y C:1.0. B depende de D:2.0. C depende de D:1.5.
Aplica la regla "nearest wins": D se resuelve a la versión más cercana al proyecto raíz.
Muestra el árbol de dependencias y la versión resuelta de D.

---

**Ejercicio 4 — Dependency scopes**
Implementa la resolución de classpath por scope:
- `compile` → aparece en compile y runtime classpath
- `test` → solo en test classpath
- `runtime` → solo en runtime classpath
- `provided` → solo en compile classpath (no en runtime)

Demo con 6 dependencias en distintos scopes. Muestra el classpath para cada modo.

---

**Ejercicio 5 — Plugin execution**
Implementa `Plugin(groupId, artifactId, goals, phase)`.
El lifecycle ejecuta automáticamente los goals de los plugins en la fase configurada.
Demo con `maven-compiler-plugin` (fase `compile`) y `maven-surefire-plugin` (fase `test`).

---

**Ejercicio 6 — POM inheritance**
Implementa `Pom(groupId, artifactId, version, parent, properties)`.
El POM hijo hereda las propiedades del padre. El hijo puede sobreescribir propiedades.
Demo con 2 niveles de herencia y 5 propiedades (algunas heredadas, algunas sobreescritas).
