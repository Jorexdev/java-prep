<div align="center">
  <a href="#"><img src="../../assets/modules/banner-07-colecciones-lista-v1.svg" width="100%" alt=""/></a>
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

**¿Cuándo usarías ArrayList vs LinkedList?**
ArrayList para casi todo: acceso por índice O(1), iteración eficiente, menos memoria. LinkedList cuando haces muchas inserciones/borrados en los extremos y no necesitas acceso aleatorio — aunque en ese caso `ArrayDeque` suele ser mejor opción.

---

**¿Qué complejidad algorítmica tiene `get()` en ArrayList vs LinkedList?**
ArrayList: O(1) — acceso directo al array interno por índice. LinkedList: O(n) — debe recorrer la lista nodo a nodo desde el inicio o el final.

---

**¿Qué es un `CopyOnWriteArrayList` y cuándo lo usarías?**
Una implementación thread-safe donde cada escritura crea una copia del array interno. Las lecturas son siempre sin bloqueo. Ideal para escenarios con muchas lecturas y pocas escrituras concurrentes (ej. listas de listeners). Costoso en escrituras frecuentes.

---

**¿Puede una `List` contener `null`?**
`ArrayList` y `LinkedList` sí permiten null. `List.of()` y `List.copyOf()` (inmutables) no — lanzan `NullPointerException`.

---

**¿Cuál es la diferencia entre `List.of()` y `new ArrayList<>()`?**
`List.of()` devuelve una lista inmutable (no se puede añadir, modificar ni eliminar) y no permite null. `new ArrayList<>()` es mutable, permite null y es modificable. Usar `List.of()` cuando la lista no va a cambiar.

---

**¿Qué peligros tiene `subList()` y cómo los evitas?**

`subList(from, to)` devuelve una vista respaldada por el ArrayList original, no una copia. Cualquier modificación estructural al ArrayList original (añadir o eliminar elementos fuera de la subList) invalida la subList y provoca `ConcurrentModificationException` al acceder a ella. Si la lista original se puede modificar, crea una copia explícita: `new ArrayList<>(list.subList(from, to))`. Además, retener una referencia a la subList impide que el ArrayList original sea recolectado por el GC aunque ya no se necesite.

---

**¿Cuándo tiene sentido usar `CopyOnWriteArrayList` en lugar de un `ArrayList` sincronizado?**

`CopyOnWriteArrayList` es ideal cuando las lecturas son la mayoría de las operaciones y las escrituras son esporádicas (ej. listas de listeners o suscriptores). Cada escritura hace una copia completa del array, lo que hace las escrituras costosas en O(n), pero garantiza que los iteradores nunca lanzan `ConcurrentModificationException` porque trabajan sobre un snapshot inmutable. Un `Collections.synchronizedList(ArrayList)` sería más eficiente si escrituras y lecturas son equilibradas, pero requiere sincronización manual al iterar.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
