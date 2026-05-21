# Ejercicios — 31 Herramientas-Git
## Fácil
Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Commit graph**
Modela un `Commit` con campos `hash`, `message`, `parent`, `author` y `timestamp`.
Construye el grafo de una rama con 6 commits encadenados.
Implementa `gitLog()` que muestra el historial en orden cronológico inverso (más reciente primero).

---

**Ejercicio 2 — Fast-forward merge**
La rama `feature` tiene 3 commits sobre `main`.
Implementa `merge(main, feature)` que mueve el puntero de `main` al último commit de `feature`.
Muestra que no se crea un commit de merge adicional.

---

**Ejercicio 3 — 3-way merge**
Las ramas `main` y `feature` divergen desde un ancestro común.
Implementa `merge()` que crea un commit con 2 padres.
Si la misma "línea" fue modificada en ambas ramas, reporta un conflicto en lugar de hacer el merge.

---

**Ejercicio 4 — Cherry-pick**
Dado un `Commit` específico, aplica sus cambios a la rama actual como un nuevo commit
con el mismo mensaje seguido de `" (cherry-pick)"`.
Demo: copiar un bugfix de la rama `hotfix` a `main`.

---

**Ejercicio 5 — Stash**
Implementa `GitStash` respaldado por un `Deque`.
`push(snapshot)` guarda cambios, `pop()` restaura el último, `list()` muestra todos los snapshots.
Demo: guardar un snapshot, simular un cambio de rama y luego restaurar.

---

**Ejercicio 6 — Git log filters**
Implementa `gitLog(commits, author, grepMessage)` que filtra el historial por autor
y por texto contenido en el mensaje.
Demo con 10 commits: filtrar por `author="alice"` y mensajes que contengan `"fix"`.
