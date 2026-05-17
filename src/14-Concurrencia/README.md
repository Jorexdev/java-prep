<div align="center">
  <a href="#"><img src="../../assets/modules/banner-14-concurrencia-v1.svg" width="100%" alt=""/></a>
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

La **concurrencia** permite que múltiples hilos ejecuten código simultáneamente. En Java, varios hilos comparten memoria (heap), lo que introduce condiciones de carrera que deben controlarse mediante sincronización.

```java
// Problema clásico: race condition
int contador = 0;
// Hilo A y Hilo B leen 0, ambos suman 1, ambos escriben 1
// Resultado esperado: 2, resultado real: 1

// Solución con synchronized
synchronized void incrementar() { contador++; }
```

Los problemas clásicos de concurrencia son: **race condition** (escritura no atómica), **deadlock** (dos hilos esperándose mutuamente), **starvation** (un hilo nunca consigue el lock) y **livelock** (hilos activos que no progresan).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Mecanismos de sincronización:**

| Mecanismo | Uso |
|---|---|
| `synchronized` | Lock implícito sobre el monitor del objeto |
| `volatile` | Garantiza visibilidad (no atomicidad de operaciones compuestas) |
| `ReentrantLock` | Lock explícito con `tryLock()`, timeout, interruptible |
| `Semaphore` | N permisos — controla acceso a recursos limitados |
| `wait() / notify()` | Comunicación entre hilos dentro de synchronized |

```java
// ReentrantLock — más flexible que synchronized
ReentrantLock lock = new ReentrantLock();
lock.lock();
try { /* sección crítica */ }
finally { lock.unlock(); }  // siempre en finally
```

**ExecutorService** — preferible a crear hilos manualmente:
```java
ExecutorService pool = Executors.newFixedThreadPool(4);
pool.submit(() -> tareaLenta());
pool.shutdown();
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Control preciso sobre secciones críticas.
- `ReentrantLock` ofrece `tryLock()` con timeout para evitar deadlocks.
- `ExecutorService` gestiona pools de hilos sin crear hilos manualmente.
- `Semaphore` para controlar acceso concurrente a recursos limitados.

Ver [ExpSynchronized.java](ExpSynchronized.java), [ExpVolatile.java](ExpVolatile.java), [ExpReentrantLock.java](ExpReentrantLock.java), [ExpSemaphore.java](ExpSemaphore.java) y [ExpWaitNotify.java](ExpWaitNotify.java).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
