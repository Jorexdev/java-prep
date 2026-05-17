<div align="center">
  <a href="#"><img src="../../assets/modules/banner-31-herramientas-git-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Cuál es la diferencia entre merge y rebase?**
`merge` crea un merge commit que une dos ramas preservando el historial original tal como ocurrió. `rebase` mueve los commits de una rama sobre la punta de otra, reescribiendo su historial — produce un historial lineal más limpio. Regla de oro: nunca hagas rebase en ramas compartidas con otros (reescribir historial público causa problemas).

---

**¿Para qué sirve `git stash`?**
Guarda temporalmente los cambios no commiteados (tracked) del working directory y el staging area, dejando el árbol limpio. Útil para cambiar de rama sin perder trabajo en progreso. `git stash pop` recupera los cambios. `git stash list` muestra todos los stashes guardados.

---

**¿Qué diferencia hay entre `git fetch` y `git pull`?**
`fetch` descarga los cambios del remoto al repositorio local pero no integra nada — puedes revisar antes de fusionar. `pull` es `fetch` + `merge` (o `rebase` con `--rebase`) en una sola operación. Preferible usar `fetch` primero para revisar lo que llegó antes de integrar.

---

**¿Cuándo usarías cherry-pick?**
Cuando necesitas aplicar un commit específico de una rama a otra sin hacer merge de toda la rama. Útil para: llevar un fix crítico de develop a una release branch, recuperar un commit perdido o aplicar selectivamente cambios de una feature sin incluir toda la feature.

---

**¿Qué es un conflict en Git y cómo lo resuelves?**
Un conflict ocurre cuando dos ramas modifican las mismas líneas de un archivo y Git no puede fusionarlas automáticamente. Git marca el archivo con marcadores (`<<<<<<<`, `=======`, `>>>>>>>`). La resolución: editar el archivo manualmente dejando el contenido correcto, luego `git add archivo` y `git commit` para completar el merge.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
