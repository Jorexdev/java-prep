<div align="center">
  <a href="#"><img src="../../assets/modules/banner-10-colecciones-cola-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

`Queue` es una colección **FIFO** (First In, First Out): el primero en entrar es el primero en salir. Se usa para modelar colas de tareas, pipelines de procesamiento o buffers productor-consumidor.

```java
Queue<String> cola = new LinkedList<>();
cola.offer("primero");   // añade al final
cola.offer("segundo");
cola.poll();   // "primero" — extrae y elimina el inicio
cola.peek();   // "segundo" — lee sin eliminar
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Implementación | Orden | Notas |
|---|---|---|
| `LinkedList` | FIFO | Implementa Queue + Deque. Permite null. |
| `ArrayDeque` | FIFO/LIFO | Más rápido que LinkedList. Sin null. Preferible. |
| `PriorityQueue` | Por prioridad | Min-heap por defecto. Null no permitido. |
| `ConcurrentLinkedQueue` | FIFO | Thread-safe, sin bloqueo. |
| `BlockingQueue` (interfaz) | FIFO/Prioridad | Bloquea en put/take. Productor-consumidor. |

**Métodos Queue — dos versiones:**

| Operación | Lanza excepción | Devuelve null/false |
|---|---|---|
| Insertar al final | `add(e)` | `offer(e)` |
| Extraer del inicio | `remove()` | `poll()` |
| Ver inicio | `element()` | `peek()` |

**PriorityQueue** ordena según el orden natural (`Comparable`) o un `Comparator`. Los elementos con menor valor tienen mayor prioridad (min-heap).

**BlockingQueue** (`ArrayBlockingQueue`, `LinkedBlockingQueue`) bloquea el hilo en `put()` si está llena y en `take()` si está vacía — ideal para el patrón productor-consumidor.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Orden FIFO natural para colas de tareas.
- PriorityQueue: procesamiento por prioridad sin ordenar manualmente.
- BlockingQueue: coordinación productor-consumidor sin sincronización manual.
- ArrayDeque como cola es más eficiente que LinkedList.

Ver [ExpArrayDeque.java](ExpArrayDeque.java), [ExpPriorityQueue.java](ExpPriorityQueue.java) y [ExpConcurrentLinkedQueue.java](ExpConcurrentLinkedQueue.java) para ejemplos.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
