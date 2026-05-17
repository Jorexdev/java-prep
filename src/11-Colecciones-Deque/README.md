<div align="center">
  <a href="#"><img src="../../assets/modules/banner-11-colecciones-deque-v1.svg" width="100%" alt=""/></a>
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

`Deque` (Double-Ended Queue) permite insertar y eliminar elementos por **ambos extremos**. Es la estructura más versátil: puede usarse como **cola FIFO** (Queue) o como **pila LIFO** (Stack).

```java
Deque<String> deque = new ArrayDeque<>();
deque.offerFirst("inicio");  // añade al frente
deque.offerLast("fin");      // añade al final
deque.pollFirst();           // extrae del frente
deque.pollLast();            // extrae del final
```

`ArrayDeque` es la implementación recomendada en casi todos los casos y **reemplaza a Stack** en el código moderno.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Implementación | Thread-safe | Null | Notas |
|---|---|---|---|
| `ArrayDeque` | No | No | La más rápida. Opción por defecto. Reemplaza Stack y Queue. |
| `LinkedList` | No | Sí | Permite null. Más memoria por nodos. |
| `ConcurrentLinkedDeque` | Sí | No | Thread-safe sin bloqueo. |

**Como Stack (LIFO):**
```java
deque.push("elemento");   // = offerFirst
deque.pop();              // = pollFirst, lanza si vacío
deque.peek();             // = peekFirst
```

**Como Queue (FIFO):**
```java
deque.offer("elemento");  // = offerLast
deque.poll();             // = pollFirst
```

**Métodos principales:**

| | Primera posición | Última posición |
|---|---|---|
| Añadir | `offerFirst` / `push` | `offerLast` / `offer` |
| Extraer | `pollFirst` / `poll` | `pollLast` |
| Ver | `peekFirst` / `peek` | `peekLast` |

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Reemplaza `Stack` con mejor rendimiento (Stack extiende Vector, sincronizado).
- Reemplaza `Queue` con mejor rendimiento que LinkedList.
- Un único tipo de dato para stack, queue y deque.
- `ArrayDeque` es más eficiente que LinkedList para ambos extremos.

Ver [ExpArrayDeque.java](ExpArrayDeque.java), [ExpLinkedListDeque.java](ExpLinkedListDeque.java) y [ExpConcurrentLinkedDeque.java](ExpConcurrentLinkedDeque.java).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
