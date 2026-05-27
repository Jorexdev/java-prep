<div align="center">
  <a href="#"><img src="../../assets/modules/banner-16-hilos-virtuales-v1.svg" width="100%" alt=""/></a>
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

**¿Qué son los Virtual Threads y en qué se diferencian de los hilos de plataforma?**
Los virtual threads son hilos gestionados por la JVM (no el OS) que tienen costo de creación mínimo y pueden crearse por millones. Los hilos de plataforma corresponden 1:1 con hilos del OS (~1MB de stack, costosos). La JVM multiplexa muchos virtual threads sobre un pool pequeño de carrier threads (hilos del OS).

---

**¿Para qué tipo de tareas son ideales los Virtual Threads?**
Para tareas I/O-bound: llamadas a bases de datos, APIs externas, lectura de archivos. En estas tareas el hilo pasa la mayor parte del tiempo bloqueado esperando. Con virtual threads, ese tiempo de espera libera el carrier thread para otro virtual thread. No mejoran tareas CPU-bound donde el límite es el número de cores.

---

**¿Qué es el "pinning" en Virtual Threads y cuándo ocurre?**
El pinning ocurre cuando un virtual thread no puede desacoplarse de su carrier thread durante una operación de bloqueo. Sucede dentro de bloques `synchronized` y en código nativo. Reduce la escalabilidad porque el carrier thread queda bloqueado. La solución es usar `ReentrantLock` en lugar de `synchronized`.

---

**¿Los Virtual Threads mejoran el rendimiento de tareas CPU-bound?**
No. Para CPU-bound, el límite es el número de cores físicos. Tener más hilos que cores solo aumenta el context-switching sin mejorar el throughput. Para CPU-bound sigue siendo mejor un `ForkJoinPool` con pocos hilos ajustados al número de cores.

---

**¿Cómo crearías un ExecutorService con Virtual Threads?**
`Executors.newVirtualThreadPerTaskExecutor()` — crea un virtual thread nuevo por cada tarea enviada. No es un pool en el sentido tradicional (no reutiliza hilos); el costo de crear virtual threads es tan bajo que no hace falta pool.

---

**¿Qué es Structured Concurrency y qué problema resuelve en Java 21?**
Structured Concurrency (JEP 453, preview en Java 21) organiza las tareas concurrentes jerárquicamente: un scope padre no termina hasta que todas sus subtareas hayan terminado, y si una falla puede cancelar las demás automáticamente. Resuelve el problema de "thread leaks" y errores difíciles de propagar de `CompletableFuture`. Con `StructuredTaskScope.ShutdownOnFailure`, si cualquier subtarea falla el scope cancela el resto y propaga el primer error al padre.

---

**¿Por qué los virtual threads no mejoran las tareas CPU-bound?**
Los virtual threads mejoran el throughput desacoplando la concurrencia lógica del número de hilos del OS — son útiles cuando los hilos pasan tiempo bloqueados (I/O) y liberan el carrier thread para otras tareas. En tareas CPU-bound el hilo nunca se bloquea: ocupa el carrier thread continuamente. Añadir más virtual threads que cores físicos solo aumenta el context-switching del scheduler sin incrementar la capacidad de cómputo real. Para CPU-bound, `ForkJoinPool` con paralelismo ajustado a los cores sigue siendo la mejor opción.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
