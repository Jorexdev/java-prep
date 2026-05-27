<div align="center">
  <a href="#"><img src="../../assets/modules/banner-14-concurrencia-v1.svg" width="100%" alt=""/></a>
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

**¿Qué es una race condition y cómo se evita?**
Una race condition ocurre cuando el resultado de una operación depende del orden de ejecución de múltiples hilos accediendo a datos compartidos. Se evita con: `synchronized` (lock implícito), `ReentrantLock` (lock explícito), variables atómicas (`AtomicInteger`), o diseñando sin estado compartido.

---

**¿Cuál es la diferencia entre `synchronized` y `ReentrantLock`?**
`synchronized` es más simple: lock automático, no olvidable. `ReentrantLock` es más flexible: `tryLock()` con timeout (evita bloqueos indefinidos), interruptible, fairness configurable, múltiples condiciones (`Condition`). Preferible cuando necesitas control fino sobre el bloqueo.

---

**¿Para qué sirve `volatile` y cuándo no es suficiente?**
`volatile` garantiza que los cambios a una variable sean visibles inmediatamente entre hilos (evita cachés de CPU). Pero no garantiza atomicidad: `contador++` son tres operaciones (leer, incrementar, escribir) que pueden intercalarse. Para eso se necesita `AtomicInteger` o `synchronized`.

---

**¿Qué es un deadlock y cómo se puede prevenir?**
Dos hilos esperan indefinidamente porque cada uno tiene el lock que el otro necesita. Prevención: adquirir siempre los locks en el mismo orden, usar `tryLock()` con timeout, o diseñar para evitar locks anidados.

---

**¿Qué diferencia hay entre `wait/notify` y `Condition`?**
`wait/notify` solo funciona dentro de bloques `synchronized` y notifica a un monitor implícito. `Condition` funciona con `ReentrantLock` y permite múltiples condiciones de espera en el mismo lock: `lock.newCondition()` devuelve objetos `Condition` independientes con `await/signal`.

---

**¿Cuándo usarías `ExecutorService` en lugar de crear hilos manualmente?**
Siempre. Crear `new Thread()` repetidamente es costoso y sin control. `ExecutorService` gestiona pools reutilizables, tiene shutdown ordenado y maneja excepciones. Tipos: `newFixedThreadPool`, `newCachedThreadPool`, `newScheduledThreadPool`, `newVirtualThreadPerTaskExecutor`.

---

**¿Qué es `LockSupport.park/unpark` y cómo lo usa internamente AQS?**
`LockSupport.park()` suspende el hilo actual sin requerir un monitor — es más ligero que `Object.wait()`. `LockSupport.unpark(thread)` lo despierta. `AbstractQueuedSynchronizer` (la base de `ReentrantLock`, `Semaphore`, `CountDownLatch`) usa estas primitivas para gestionar su cola de hilos bloqueados (CLH queue): cuando un hilo no puede adquirir el lock, se encola y se pone en park; cuando el lock se libera, el AQS hace unpark al sucesor de la cola.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
