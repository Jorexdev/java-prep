<div align="center">
  <a href="#"><img src="../../assets/modules/banner-12-colecciones-concurrentes-v1.svg" width="100%" alt=""/></a>
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

Las colecciones concurrentes del paquete `java.util.concurrent` son **thread-safe** sin requerir sincronización manual externa. Están diseñadas para alta concurrencia con mayor rendimiento que las alternativas clásicas (`synchronized`, `Collections.synchronizedMap()`).

El enfoque clásico sincroniza toda la estructura:
```java
Map<String, Integer> synced = Collections.synchronizedMap(new HashMap<>());
// Bloquea todo el mapa en cada operación
```

Las colecciones concurrentes usan estrategias más finas (CAS, segmentación, copy-on-write) para minimizar la contención.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

| Colección | Estrategia | Caso de uso |
|---|---|---|
| `ConcurrentHashMap` | CAS + lock por segmento | Mapa compartido entre hilos |
| `CopyOnWriteArrayList` | Copia en escritura | Lista con muchas lecturas, pocas escrituras |
| `ConcurrentLinkedQueue` | CAS (lock-free) | Cola FIFO sin bloqueo |
| `BlockingQueue` | Lock + condición | Productor-consumidor |
| `ConcurrentSkipListMap/Set` | Skip list | Mapa/Set ordenado thread-safe |

**ConcurrentHashMap:** No bloquea todo el mapa. Java 8+ usa CAS por bucket. No permite null en clave ni valor (ambigüedad con ausencia). Operaciones atómicas: `putIfAbsent`, `computeIfAbsent`, `merge`.

**CopyOnWriteArrayList:** Cada escritura crea una copia del array. Las lecturas no necesitan lock y ven la versión anterior hasta que la escritura termina. Útil para listeners y configuración que se lee mucho y se modifica poco.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Thread-safety sin `synchronized` en cada operación.
- Mayor rendimiento que `synchronized*` bajo alta concurrencia.
- Operaciones atómicas integradas (putIfAbsent, computeIfAbsent...).
- BlockingQueue simplifica el patrón productor-consumidor.

Ver [ExpConcurrentHashMap.java](ExpConcurrentHashMap.java), [ExpCopyOnWriteArrayList.java](ExpCopyOnWriteArrayList.java), [ExpBlockingQueue.java](ExpBlockingQueue.java) y [ExpConcurrentCollections.java](ExpConcurrentCollections.java).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
