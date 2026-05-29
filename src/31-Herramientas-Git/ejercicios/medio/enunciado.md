# Ejercicios — 31 Herramientas-Git
## Medio
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Rebase**
Toma los commits de la rama `feature` y re-aplícalos sobre el HEAD de `main`.
Los commits resultantes tienen nuevos hashes pero el mismo mensaje.
Demo: `feature` tiene 3 commits, `main` tiene 2 commits nuevos desde la bifurcación.
Muestra el historial antes y después del rebase.

---

**Ejercicio 2 — Conflict resolution**
En un 3-way merge, genera marcadores de conflicto en las líneas conflictivas:
```
<<<< HEAD
valor-de-main
====
valor-de-feature
>>>> feature
```
Implementa `ConflictResolver.ours()` que acepta la versión HEAD y `ConflictResolver.theirs()`
que acepta la versión de feature. Muestra el estado antes y después de resolver.

---

**Ejercicio 3 — Git bisect**
Dada una lista de `Commit` donde cada uno puede tener o no un bug,
implementa `bisect(good, bad)` que hace búsqueda binaria para encontrar
el primer commit que introdujo el bug.
Muestra los pasos: qué commits se comprueban y cuál es el resultado final.

---

**Ejercicio 4 — Interactive rebase**
Implementa las operaciones de rebase interactivo sobre una lista de 5 commits:
- `pick` — mantener el commit tal cual
- `squash` — fusionar con el commit anterior, concatenando los mensajes
- `reword` — cambiar el mensaje del commit
- `drop` — eliminar el commit

Muestra el historial antes y después de aplicar el plan de rebase.

---

**Ejercicio 5 — GitFlow validation**
Implementa las reglas de GitFlow en `validateMerge(from, to)`:
- `hotfix/*` solo puede mergear a `main` y `develop`
- `feature/*` solo puede mergear a `develop`
- `release/*` solo puede mergear a `main` y `develop`

Si la merge viola las reglas, lanza una excepción descriptiva.
Demo con merges válidos e inválidos.

---

**Ejercicio 6 — Interactive rebase: squash, reword, drop**
Dado un historial de 6 commits con mensajes y contenidos, implementa un interactive rebase con plan: `pick c1`, `squash c2` (fusiona con c1, concatena mensajes), `reword c3` (cambia el mensaje a "feat: módulo login refactorizado"), `pick c4`, `drop c5`, `pick c6`. El resultado debe tener 4 commits con los mensajes y contenidos correctos. Muestra el historial antes y después, indicando qué operación se aplicó a cada commit original.

---
