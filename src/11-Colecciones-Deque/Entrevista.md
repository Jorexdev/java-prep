<div align="center">
  <a href="#"><img src="../../assets/modules/banner-11-colecciones-deque-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre Deque y Queue?**
Queue solo permite operaciones por un extremo: insertar al final, extraer del inicio (FIFO). Deque permite insertar y extraer por ambos extremos, lo que le da la flexibilidad de funcionar como cola (FIFO), pila (LIFO) o estructura de doble entrada.

---

**¿Por qué ArrayDeque es preferible a Stack?**
`Stack` extiende `Vector`, que está sincronizado (overhead innecesario en contexto single-thread) y tiene métodos heredados que no tienen sentido en una pila. `ArrayDeque` es más rápido, no está sincronizado innecesariamente y tiene una API más limpia con `push/pop/peek`.

---

**¿Puede ArrayDeque contener null?**
No — lanza `NullPointerException`. Esto es intencional: null se usa como valor centinela en los métodos como `peek()` (retorna null si está vacío), y permitir null como elemento crearía ambigüedad. `LinkedList` sí permite null si se necesita.

---

**¿Cuándo usarías Deque como stack vs como queue?**
Como stack cuando el orden de procesamiento es LIFO: historial de navegación, undo/redo, evaluación de expresiones, DFS (Depth-First Search). Como queue cuando el orden es FIFO: sistemas de mensajería, BFS (Breadth-First Search), procesamiento por orden de llegada.

---

**¿Qué diferencia hay entre `offerFirst()` y `push()`?**
`push(e)` es equivalente a `offerFirst(e)` — ambos insertan al frente. La diferencia semántica: `push` viene del vocabulario de stack (LIFO), `offerFirst` viene del vocabulario de deque (doble extremo). `push` lanza excepción si falla (capacidad); en ArrayDeque son equivalentes.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
