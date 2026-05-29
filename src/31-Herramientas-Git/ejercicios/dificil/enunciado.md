# Ejercicios — 31 Herramientas-Git
## Difícil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Content hash**
Implementa `Blob(content)` cuyo hash se calcula como `content.hashCode()` (simulando SHA-256).
Implementa `Tree` con un mapa `name → Blob`.
Demuestra que dos ramas con el mismo contenido de archivos producen el mismo tree hash,
aunque tengan historiales de commits distintos.

---

**Ejercicio 2 — Reflog**
Implementa `Reflog(List<ReflogEntry>)` donde cada operación (checkout, reset, merge, commit)
añade una entrada con `(action, hash, timestamp)`.
`gitReflog()` muestra las últimas 10 entradas.
`reset(n)` restaura el estado de `n` operaciones atrás.
Demo con una secuencia de operaciones y una recuperación con `reset`.

---

**Ejercicio 3 — Pre-commit hook**
Implementa `HookRunner` con una lista de `Hook(name, Predicate<CommitData>)`.
Antes de cada commit, ejecuta todos los hooks en orden.
Si alguno falla, bloquea el commit con un mensaje descriptivo.
Demo con dos hooks: "no TODO en el código" y "mensaje mínimo de 10 caracteres".

---

**Ejercicio 4 — Sparse checkout**
Implementa `SparseCheckout(List<String> patterns)` con soporte para glob simple:
`*.java` y `src/**`.
Dado un árbol de 20 archivos con rutas variadas, materializa solo los que coincidan
con los patterns configurados.
Demo con 2 patterns activos: muestra qué archivos se incluyen y cuáles se excluyen.

---

**Ejercicio 5 — Git bisect automático**
Implementa `GitBisect` que recibe una lista ordenada de commits (del más antiguo al más nuevo) y una función `Predicate<Commit> testFn` que devuelve `true` si el commit tiene el bug. `bisect()` realiza búsqueda binaria: marca el commit central como good o bad según `testFn`, descartando la mitad del rango en cada paso. Muestra en cada iteración qué commit se está probando, el resultado, y el rango restante. El resultado final es el primer commit que introdujo el bug. Demo con 16 commits donde el bug se introduce en el commit 11.

---
